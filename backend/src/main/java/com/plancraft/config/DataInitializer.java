package com.plancraft.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.plancraft.module.user.entity.User;
import com.plancraft.module.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 应用启动时执行：将数据库中未加密的密码自动 BCrypt 加密
 * 只处理明文密码（BCrypt 密码以 $2a$ 开头，不会被重复处理）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        List<User> users = userMapper.selectList(new LambdaQueryWrapper<>());
        boolean updated = false;

        for (User user : users) {
            String pwd = user.getPassword();
            // BCrypt 密文以 $2a$ 或 $2b$ 开头，明文则需要加密
            if (pwd != null && !pwd.startsWith("$2a$") && !pwd.startsWith("$2b$")) {
                user.setPassword(passwordEncoder.encode(pwd));
                userMapper.updateById(user);
                updated = true;
                log.info("[DataInitializer] 已加密用户密码: {}", user.getUsername());
            }
        }

        if (updated) {
            log.info("[DataInitializer] 密码初始化完成，后续启动将跳过");
        } else {
            log.info("[DataInitializer] 所有用户密码已是加密状态，无需处理");
        }
    }
}
