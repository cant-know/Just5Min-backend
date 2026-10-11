package com.example.just5minbackend.service.impl;

import com.example.just5minbackend.common.BusinessException;
import com.example.just5minbackend.common.ResultCode;
import com.example.just5minbackend.dto.UpdateProfileDTO;
import com.example.just5minbackend.entity.User;
import com.example.just5minbackend.mapper.UserMapper;
import com.example.just5minbackend.service.UserProfileService;
import com.example.just5minbackend.vo.UserProfileVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Base64;

/**
 * 「我的资料」读写。
 *
 * <p>头像直接以 Base64 DataURL 存库（见 {@code db/profile_migration.sql}），
 * 因此这里必须自行校验格式与体积（{@code @Valid} 管不到 base64 内容）。</p>
 *
 * <p>资料字段的<b>三态语义</b>：DTO 字段为 {@code null} → 不改动；空串 → 清空；
 * 有值 → 更新。这样前端「只提交改动字段」时不会误清空其它字段。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {

    /** 头像解码后的字节上限：200KB */
    private static final int AVATAR_MAX_BYTES = 200 * 1024;

    private static final String JPEG_PREFIX = "data:image/jpeg;base64,";
    private static final String PNG_PREFIX = "data:image/png;base64,";

    /** 生日合理区间：太早/未来日期都视为误填 */
    private static final LocalDate MIN_BIRTHDAY = LocalDate.of(1900, 1, 1);

    /** 考研年份合理区间（今年往前留点余量，往后留足备考周期） */
    private static final int MIN_EXAM_YEAR = 2000;
    private static final int MAX_EXAM_YEAR = 2100;

    private final UserMapper userMapper;

    @Override
    public UserProfileVO get(Long userId) {
        User user = userMapper.selectProfileById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
        }
        return toVO(user);
    }

    @Override
    @Transactional
    public UserProfileVO update(Long userId, UpdateProfileDTO dto) {
        // ---- 必填项：昵称不允许清空（前端也拦了，这里兜底防绕过）
        String nickname = null;
        if (dto.getNickname() != null) {
            nickname = dto.getNickname().trim();
            if (nickname.isEmpty()) {
                throw new BusinessException(ResultCode.PARAM_ERROR, "昵称不能为空");
            }
            if (nickname.length() > 64) {
                throw new BusinessException(ResultCode.PARAM_ERROR, "昵称最长 64 个字符");
            }
        }

        // ---- 头像
        String avatarUrl = null;
        if (dto.getAvatarUrl() != null && !dto.getAvatarUrl().isBlank()) {
            avatarUrl = dto.getAvatarUrl().trim();
            validateAvatar(avatarUrl);
        }

        // ---- 可清空的文本字段：统一 trim，空串保留（表示清空），null 表示不动
        String signature = null;
        if (dto.getSignature() != null) {
            signature = normalizeText(dto.getSignature(), 64, "个性签名");
        }
        String region = null;
        if (dto.getRegion() != null) {
            region = normalizeText(dto.getRegion(), 32, "地区");
        }
        String targetSchool = null;
        if (dto.getTargetSchool() != null) {
            targetSchool = normalizeText(dto.getTargetSchool(), 64, "目标院校");
        }
        String targetMajor = null;
        if (dto.getTargetMajor() != null) {
            targetMajor = normalizeText(dto.getTargetMajor(), 64, "报考专业");
        }

        // ---- 性别：0/1/2
        Integer gender = null;
        if (dto.getGender() != null) {
            gender = dto.getGender();
            if (gender < 0 || gender > 2) {
                throw new BusinessException(ResultCode.PARAM_ERROR, "性别只能是 0未知 / 1男 / 2女");
            }
        }

        // ---- 生日：空串 = 清空；有值必须 yyyy-MM-dd 且在合理区间
        LocalDate birthday = null;
        boolean clearBirthday = false;
        if (dto.getBirthday() != null) {
            String raw = dto.getBirthday().trim();
            if (raw.isEmpty()) {
                clearBirthday = true;
            } else {
                birthday = parseBirthday(raw);
            }
        }

        // ---- 考研年份：空串 = 清空；有值必须数字且在合理区间
        Integer examYear = null;
        boolean clearExamYear = false;
        if (dto.getExamYear() != null) {
            String raw = dto.getExamYear().trim();
            if (raw.isEmpty()) {
                clearExamYear = true;
            } else {
                examYear = parseExamYear(raw);
            }
        }

        boolean nothingToUpdate = nickname == null && avatarUrl == null && signature == null
                && gender == null && region == null && targetSchool == null && targetMajor == null
                && birthday == null && !clearBirthday && examYear == null && !clearExamYear;
        if (nothingToUpdate) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "没有需要更新的内容");
        }

        // updateProfile 是动态 SET：null 字段不动，空串会写入实现「清空」
        userMapper.updateProfile(userId, nickname, avatarUrl, signature, gender, region,
                targetSchool, targetMajor, birthday, clearBirthday, examYear, clearExamYear);
        return get(userId);
    }

    // ------------------------------------------------------------------ 校验

    private void validateAvatar(String avatar) {
        if (!avatar.startsWith(JPEG_PREFIX) && !avatar.startsWith(PNG_PREFIX)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "头像格式不支持，仅支持 jpeg/png");
        }
        byte[] bytes;
        try {
            bytes = Base64.getDecoder().decode(avatar.substring(avatar.indexOf(',') + 1));
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "头像数据非法");
        }
        if (bytes.length > AVATAR_MAX_BYTES) {
            throw new BusinessException(ResultCode.AVATAR_TOO_LARGE,
                    "头像图片过大（" + (bytes.length / 1024) + "KB），请重新选择");
        }
    }

    /**
     * 文本字段归一化：trim 后长度超限报错。
     * 返回空串表示「清空」（调用方不要把它转成 null，否则动态 SET 会忽略掉）。
     */
    private String normalizeText(String value, int maxLength, String label) {
        String trimmed = value.trim();
        if (trimmed.length() > maxLength) {
            throw new BusinessException(ResultCode.PARAM_ERROR, label + "最长 " + maxLength + " 个字符");
        }
        return trimmed;
    }

    private LocalDate parseBirthday(String raw) {
        LocalDate date;
        try {
            date = LocalDate.parse(raw);
        } catch (DateTimeParseException e) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "生日格式应为 yyyy-MM-dd");
        }
        if (date.isBefore(MIN_BIRTHDAY) || date.isAfter(LocalDate.now())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "生日不在合理范围内");
        }
        return date;
    }

    private Integer parseExamYear(String raw) {
        int year;
        try {
            year = Integer.parseInt(raw);
        } catch (NumberFormatException e) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "考研年份应为数字");
        }
        if (year < MIN_EXAM_YEAR || year > MAX_EXAM_YEAR) {
            throw new BusinessException(ResultCode.PARAM_ERROR,
                    "考研年份应在 " + MIN_EXAM_YEAR + "~" + MAX_EXAM_YEAR + " 之间");
        }
        return year;
    }

    // ------------------------------------------------------------------ 组装

    private UserProfileVO toVO(User user) {
        String nickname = user.getNickname();
        if (nickname == null || nickname.isBlank()) {
            // 微信静默登录用户没有昵称，给个稳定兜底，前端可直接展示
            nickname = "用户" + user.getId();
        }
        return new UserProfileVO(
                user.getId(),
                nickname,
                user.getPhone(),
                user.getAvatarUrl(),
                blankToNull(user.getSignature()),
                user.getGender() == null ? 0 : user.getGender(),
                user.getBirthday(),
                blankToNull(user.getRegion()),
                blankToNull(user.getTargetSchool()),
                blankToNull(user.getTargetMajor()),
                user.getExamYear(),
                user.getPoints() == null ? 0 : user.getPoints()
        );
    }

    /**
     * 空串统一回传成 null，跟 birthday/examYear 的「未设置」表示保持一致。
     * 库里存空串只是为了让动态 SET 能命中「清空」这个意图（见 updateProfile 注释）。
     */
    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
