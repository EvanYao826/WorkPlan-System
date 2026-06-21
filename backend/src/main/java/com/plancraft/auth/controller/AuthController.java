package com.plancraft.auth.controller;

import com.plancraft.auth.dto.LoginRequest;
import com.plancraft.auth.dto.LoginResponse;
import com.plancraft.auth.util.JwtUtil;
import com.plancraft.common.result.R;
import com.plancraft.module.user.entity.User;
import com.plancraft.module.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@Tag(name = "认证管理", description = "登录认证相关接口")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserService userService;

    @Operation(summary = "用户登录", description = "用户名密码登录，返回 JWT Token")
    @PostMapping("/login")
    public R<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        // 1. 认证（Spring Security 校验用户名密码）
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        // 2. 认证通过，查完整用户信息
        User user = userService.getByUsername(request.getUsername());

        // 3. 生成 token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());

        // 4. 更新最后登录时间
        user.setLastLoginTime(LocalDateTime.now());
        userService.updateById(user);

        // 5. 返回
        return R.ok(LoginResponse.builder()
                .token(token)
                .userId(user.getId())
                .username(user.getUsername())
                .name(user.getName())
                .role(user.getRole())
                .build());
    }
}
