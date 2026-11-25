package com.music.rental.config;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.music.rental.entity.SysUser;
import com.music.rental.mapper.SysUserMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 启动时把 SQL 里的明文初始口令统一 BCrypt 化。
 * 生产环境不会这样写，此处仅为保证初始化脚本可读、且账号能直接登录。
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final SysUserMapper userMapper;
    private final PasswordEncoder encoder;

    public DataInitializer(SysUserMapper userMapper, PasswordEncoder encoder) {
        this.userMapper = userMapper;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {
        List<SysUser> users = userMapper.selectList(new QueryWrapper<>());
        for (SysUser u : users) {
            String pwd = u.getPassword();
            // 只处理非 BCrypt 值，重启不会重复加密
            if (pwd != null && !pwd.startsWith("$2a$") && !pwd.startsWith("$2b$")) {
                u.setPassword(encoder.encode(pwd));
                userMapper.updateById(u);
            }
        }
    }
}
