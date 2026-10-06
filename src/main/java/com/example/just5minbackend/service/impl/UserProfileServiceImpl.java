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

import java.util.Base64;

/**
 * 「我的资料」读写。
 * <p>头像直接以 Base64 DataURL 存库（见 {@code db/profile_migration.sql}），
 * 因此这里必须自行校验格式与体积（{@code @Valid} 管不到 base64 内容）。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {

    /** 头像解码后的字节上限：200KB */
    private static final int AVATAR_MAX_BYTES = 200 * 1024;

    private static final String JPEG_PREFIX = "data:image/jpeg;base64,";
    private static final String PNG_PREFIX = "data:image/png;base64,";

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

        String avatarUrl = null;
        if (dto.getAvatarUrl() != null && !dto.getAvatarUrl().isBlank()) {
            avatarUrl = dto.getAvatarUrl().trim();
            validateAvatar(avatarUrl);
        }

        if (nickname == null && avatarUrl == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "没有需要更新的内容");
        }

        // updateProfile 是动态 SET，只更新非 null 字段
        userMapper.updateProfile(userId, nickname, avatarUrl);
        return get(userId);
    }

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
                user.getPoints() == null ? 0 : user.getPoints()
        );
    }
}
