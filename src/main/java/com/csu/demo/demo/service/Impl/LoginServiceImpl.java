package com.csu.demo.demo.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.csu.demo.demo.domain.User;
import com.csu.demo.demo.mapper.UserMapper;
import com.csu.demo.demo.service.LoginService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.UUID;

@Service
public class LoginServiceImpl implements LoginService {

    private static final String TOKEN_PREFIX = "auth:token:";
    private static final Duration TOKEN_TTL = Duration.ofHours(2);

    private final UserMapper userMapper;
    private final StringRedisTemplate stringRedisTemplate;

    public LoginServiceImpl(UserMapper userMapper, StringRedisTemplate stringRedisTemplate) {
        this.userMapper = userMapper;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public String register(User user) {
        if (user.getUsername() == null || user.getPassword() == null) {
            throw new IllegalArgumentException("用户名和密码不能为空");
        }
        validateUsernameUnique(user.getUsername());
        user.setPassword(hashPassword(user.getPassword()));
        userMapper.insert(user);
        return generateAndCacheToken(user.getId());
    }

    @Override
    public String login(String username, String password) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        User stored = userMapper.selectOne(wrapper);
        if (stored == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        if (!stored.getPassword().equals(hashPassword(password))) {
            throw new IllegalArgumentException("密码错误");
        }
        return generateAndCacheToken(stored.getId());
    }

    @Override
    public void logout(String token) {
        if (token == null) {
            return;
        }
        stringRedisTemplate.delete(TOKEN_PREFIX + token);
    }

    private void validateUsernameUnique(String username) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        Long count = userMapper.selectCount(wrapper);
        if (count != null && count > 0) {
            throw new DuplicateKeyException("用户名已存在");
        }
    }

    private String hashPassword(String raw) {
        return DigestUtils.md5DigestAsHex(raw.getBytes(StandardCharsets.UTF_8));
    }

    private String generateAndCacheToken(int userId) {
        String token = UUID.randomUUID().toString();
        stringRedisTemplate.opsForValue()
            .set(TOKEN_PREFIX + token, String.valueOf(userId), TOKEN_TTL);
        return token;
    }
}
