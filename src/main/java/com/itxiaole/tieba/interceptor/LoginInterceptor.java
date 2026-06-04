package com.itxiaole.tieba.interceptor;

import com.itxiaole.tieba.until.JwtUtil;
import com.itxiaole.tieba.until.UserHolder;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component // 交给 Spring 管理，这样才能使用 @Autowired 和 @Value
public class LoginInterceptor implements HandlerInterceptor {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    // 自动读取你在 application.yml 里的配置
    @Value("${jwt.secret-key}")
    private String secretKey;

    @Value("${jwt.token-name}")
    private String tokenName;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 0. 放行 OPTIONS 请求（CORS 预检）
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }

        // 1. 从请求头中获取 Token（名字就是你在 yml 里配的 "token"）
        String token = request.getHeader(tokenName);

        // 2. 如果没带 Token，直接拦截打回
        if (token == null || token.isEmpty()) {
            response.setStatus(401);
            return false;
        }

        try {
            // 3. 使用之前的工具类解析 Token
            Claims claims = JwtUtil.parseJWT(secretKey, token);
            Long userId = claims.get("userId", Long.class);

            // 4. 【高阶操作】去 Redis 检查这个 Token 的状态
            String redisKey = "login:token:" + userId;
            String redisToken = stringRedisTemplate.opsForValue().get(redisKey);
            
            // 如果 Redis 里没有，或者和传过来的不一样（比如在别处登录被顶号了）
            if (redisToken == null || !redisToken.equals(token)) {
                response.setStatus(401);
                return false;
            }

            // 5. 检验全部通过！把 userId 存入当前线程，放行请求
            UserHolder.saveUser(userId);
            return true;

        } catch (Exception e) {
            // 解析报错（过期或被篡改），拦截打回
            response.setStatus(401);
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserHolder.removeUser();
    }
}