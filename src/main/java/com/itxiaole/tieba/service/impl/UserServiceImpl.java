package com.itxiaole.tieba.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itxiaole.tieba.entity.DTO.UserDTO;
import com.itxiaole.tieba.entity.User;
import com.itxiaole.tieba.mapper.UserMapper;
import com.itxiaole.tieba.service.IUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itxiaole.tieba.until.CodeUtil;
import com.itxiaole.tieba.until.JwtUtil;
import com.itxiaole.tieba.until.MailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @author xiaole
 * @since 2026-06-02
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Autowired
    private MailService mailService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public void yzm(User user) {
        // 1. 生成 6 位随机验证码
        String code = CodeUtil.generateCode();

        // 2. 将验证码存入 Redis，有效期设为 5 分钟
        // Key 规范设计：模块名:业务名:唯一标识
        String redisKey = "register:code:" + user.getUserEmail();
        stringRedisTemplate.opsForValue().set(redisKey, code, 5, TimeUnit.MINUTES);

        // 3. 发送邮件给用户
        mailService.sendSimpleMail(
                user.getUserEmail(),
                "【乐光社区】注册验证码",
                "您的注册验证码是：" + code + "，有效期5分钟。打死也不要告诉别人哦！"
        );
    }

    @Override
    public User login(User user) {
        User userDB = this.getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, user.getUsername()));
        if(userDB == null){
            throw new RuntimeException("用户不存在！");
        }
        if (!userDB.getPassword().equals(user.getPassword())){
            throw new RuntimeException("密码错误！");
        }
        if (userDB.getExperience() < 0){
            throw new RuntimeException("用户违反社区规范被封禁！");
        }
        return userDB;
    }

    @Override
    public void register(UserDTO user) {
        // 1. 拼接查 Redis 用的 Key
        String redisKey = "register:code:" + user.getUserEmail();

        // 2. 从 Redis 取出刚刚存进去的验证码
        String cachedCode = stringRedisTemplate.opsForValue().get(redisKey);

        // 3. 核心校验：验证码是否过期或错误
        if (cachedCode == null || !cachedCode.equals(user.getToken())) {
            throw new RuntimeException("验证码错误或已失效，请重新获取！");
        }

        // 4. 校验通过，必须立刻删除 Redis 中的验证码，防止被黑客无限次重复使用
        stringRedisTemplate.delete(redisKey);

        // 5. 业务校验：检查该邮箱和用户名是否已经被别人注册过了
        LambdaQueryWrapper<User> emailWrapper = new LambdaQueryWrapper<>();
        emailWrapper.eq(User::getUserEmail, user.getUserEmail());
        if (this.count(emailWrapper) > 0) {
            throw new RuntimeException("该邮箱已被注册，请直接登录！");
        }

        LambdaQueryWrapper<User> usernameWrapper = new LambdaQueryWrapper<>();
        usernameWrapper.eq(User::getUsername, user.getUsername());
        if (this.count(usernameWrapper) > 0) {
            throw new RuntimeException("该用户名已被占用，请换一个试试！");
        }

        // 6. 初始化经验值，可以在这里 set 进去
         user.setExperience(0);
        // user.setAvatar("https://默认头像地址.jpg");

        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());


        // 7. 插入数据库
        this.save(user);
    }
}
