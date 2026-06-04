package com.itxiaole.tieba.controller;

import com.itxiaole.tieba.entity.DTO.UserDTO;
import com.itxiaole.tieba.entity.Result;
import com.itxiaole.tieba.entity.User;
import com.itxiaole.tieba.service.IUserService;
import com.itxiaole.tieba.service.impl.UserServiceImpl;
import com.itxiaole.tieba.until.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 用户表 前端控制器
 * </p>
 *
 * @author xiaole
 * @since 2026-06-02
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    IUserService userService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Value("${jwt.secret-key}")
    private String secretKey;

    @Value("${jwt.expire-time}")
    private long expireTime;

    @Value("${jwt.token-name}")
    private String tokenName;

    @PostMapping("/yzm")
    public Result<String> yzm(@RequestBody User user) {
        userService.yzm(user);
        return Result.success();
    }
    @PostMapping("/register")
    public Result<String> register(@RequestBody UserDTO user) {
        userService.register(user);
        return Result.success();
    }
    @PostMapping("/login")
    public Result<String> login(@RequestBody User user) {
        User userDB = userService.login(user);

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userDB.getId());
        claims.put("username", userDB.getUsername());

        String token = JwtUtil.createJWT(secretKey, expireTime, claims);

        // 存入 Redis 并返回给前端
        stringRedisTemplate.opsForValue().set("login:token:" + userDB.getId(), token, 2, TimeUnit.HOURS);
        return Result.success(token);
    }

    @GetMapping("/info")
    public Result<User> getInfo() {
        Long userId = com.itxiaole.tieba.until.UserHolder.getUser();
        User user = userService.getById(userId);
        if (user != null) {
            user.setPassword(null); // 安全起见，不返回密码
            return Result.success(user);
        }
        return Result.error("未找到用户信息");
    }

    @PutMapping("/update")
    public Result<String> updateProfile(@RequestBody User user) {
        Long userId = com.itxiaole.tieba.until.UserHolder.getUser();
        User userDB = userService.getById(userId);
        
        if (user.getUsername() != null && !user.getUsername().isEmpty()) {
            userDB.setUsername(user.getUsername());
        }
        
        userService.updateById(userDB);
        return Result.success("资料更新成功");
    }

    @PostMapping("/resetPwd")
    public Result<String> resetPwd(@RequestParam String password, @RequestParam String code) {
        Long userId = com.itxiaole.tieba.until.UserHolder.getUser();
        User userDB = userService.getById(userId);
        
        // 1. 校验验证码
        String redisCode = stringRedisTemplate.opsForValue().get("yzm:" + userDB.getUserEmail());
        if (redisCode == null || !redisCode.equals(code)) {
            return Result.error("验证码错误或已过期");
        }
        
        // 2. 修改密码
        userDB.setPassword(password); // 建议此处也进行加密处理，保持与注册逻辑一致
        userService.updateById(userDB);
        
        // 3. 删除验证码并强制下线
        stringRedisTemplate.delete("yzm:" + userDB.getUserEmail());
        stringRedisTemplate.delete("login:token:" + userId);
        
        return Result.success("密码修改成功，请重新登录");
    }
}
