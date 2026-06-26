package com.example.consumptionplan.module.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.consumptionplan.module.user.dto.ChangePasswordDTO;
import com.example.consumptionplan.module.user.dto.LoginDTO;
import com.example.consumptionplan.module.user.dto.RegisterDTO;
import com.example.consumptionplan.module.user.dto.UpdateUserDTO;
import com.example.consumptionplan.module.user.entity.User;
import com.example.consumptionplan.module.user.mapper.UserMapper;
import com.example.consumptionplan.module.user.service.UserService;
import com.example.consumptionplan.module.user.vo.LoginVO;
import com.example.consumptionplan.module.user.vo.UserVO;
import com.example.consumptionplan.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    @Override
    public void register(RegisterDTO dto) {
        if (userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, dto.getUsername())) > 0) {
            throw new RuntimeException("用户名已存在");
        }
        if (userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getEmail, dto.getEmail())) > 0) {
            throw new RuntimeException("邮箱已存在");
        }
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setStatus(1);
        userMapper.insert(user);
    }

    @Override
    public LoginVO login(LoginDTO dto) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, dto.getUsername()));
        if (user == null || !passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }
        if (user.getStatus() == 0) {
            throw new RuntimeException("账号已被禁用");
        }
        String token = jwtUtils.generateToken(user.getId());
        return new LoginVO(token, user.getUsername(), user.getAvatar());
    }

    @Override
    public UserVO getUserInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setEmail(user.getEmail());
        vo.setAvatar(user.getAvatar());
        vo.setCreateTime(user.getCreateTime());
        return vo;
    }

    @Override
    public void updateUserInfo(Long userId, UpdateUserDTO dto) {
        User existing = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, dto.getUsername())
                .ne(User::getId, userId));
        if (existing != null) {
            throw new RuntimeException("用户名已存在");
        }
        User user = new User();
        user.setId(userId);
        user.setUsername(dto.getUsername());
        user.setAvatar(dto.getAvatar());
        userMapper.updateById(user);
    }

    @Override
    public void changePassword(Long userId, ChangePasswordDTO dto) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("原密码错误");
        }
        if (dto.getNewPassword().equals(dto.getOldPassword())) {
            throw new RuntimeException("新密码不能与原密码相同");
        }
        User update = new User();
        update.setId(userId);
        update.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userMapper.updateById(update);
    }
}
