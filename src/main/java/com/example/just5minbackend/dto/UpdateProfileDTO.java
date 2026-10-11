package com.example.just5minbackend.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 编辑资料入参。
 *
 * <p>所有字段都是可选的：只传哪个就改哪个（至少传一个，否则 Service 抛参数错误）。</p>
 *
 * <p><b>清空语义</b>：文本字段传空串表示清空（如把个性签名删掉）；
 * {@code birthday} / {@code examYear} 传空串表示清空（前端 picker 的「清除」选项回传空串）。
 * 两者都在 Service 层归一化，字段为 null 一律视为「不改动」。</p>
 */
@Data
public class UpdateProfileDTO {

    /** 对齐 user.nickname VARCHAR(64) */
    @Size(max = 64, message = "昵称最长 64 个字符")
    private String nickname;

    /** Base64 DataURL，如 data:image/jpeg;base64,...（格式与尺寸由 Service 校验） */
    private String avatarUrl;

    /** 个性签名，对齐 user.signature VARCHAR(64) */
    @Size(max = 64, message = "个性签名最长 64 个字符")
    private String signature;

    /** 性别：0未知 1男 2女（超出范围由 Service 校验） */
    private Integer gender;

    /** 生日 yyyy-MM-dd；空串表示清空 */
    private String birthday;

    /** 所在地区，对齐 user.region VARCHAR(32) */
    @Size(max = 32, message = "地区最长 32 个字符")
    private String region;

    /** 目标院校，对齐 user.target_school VARCHAR(64) */
    @Size(max = 64, message = "目标院校最长 64 个字符")
    private String targetSchool;

    /** 报考专业，对齐 user.target_major VARCHAR(64) */
    @Size(max = 64, message = "报考专业最长 64 个字符")
    private String targetMajor;

    /** 考研年份，如 2027；空串表示清空 */
    private String examYear;
}
