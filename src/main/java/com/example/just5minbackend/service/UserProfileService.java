package com.example.just5minbackend.service;

import com.example.just5minbackend.dto.UpdateProfileDTO;
import com.example.just5minbackend.vo.UserProfileVO;

public interface UserProfileService {

    /** 读取当前用户资料（含头像） */
    UserProfileVO get(Long userId);

    /**
     * 更新资料：昵称与头像都是可选的，只更新传入的字段。
     *
     * @return 更新后的最新资料
     */
    UserProfileVO update(Long userId, UpdateProfileDTO dto);
}
