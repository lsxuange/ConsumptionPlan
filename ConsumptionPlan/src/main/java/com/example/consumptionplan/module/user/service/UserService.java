package com.example.consumptionplan.module.user.service;

import com.example.consumptionplan.module.user.dto.ChangePasswordDTO;
import com.example.consumptionplan.module.user.dto.LoginDTO;
import com.example.consumptionplan.module.user.dto.RegisterDTO;
import com.example.consumptionplan.module.user.dto.UpdateUserDTO;
import com.example.consumptionplan.module.user.vo.LoginVO;
import com.example.consumptionplan.module.user.vo.UserVO;

public interface UserService {

    void register(RegisterDTO dto);

    LoginVO login(LoginDTO dto);

    UserVO getUserInfo(Long userId);

    void updateUserInfo(Long userId, UpdateUserDTO dto);

    void changePassword(Long userId, ChangePasswordDTO dto);
}
