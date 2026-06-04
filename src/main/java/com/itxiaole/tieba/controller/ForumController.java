package com.itxiaole.tieba.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itxiaole.tieba.entity.Forum;
import com.itxiaole.tieba.entity.Result;
import com.itxiaole.tieba.service.IForumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 贴吧（板块）表 前端控制器
 * </p>
 *
 * @author xiaole
 * @since 2026-06-02
 */
@RestController
@RequestMapping("/forum")
public class ForumController {

    @Autowired
    IForumService forumService;

    @Autowired
    private com.itxiaole.tieba.service.IUserService userService;

    @PostMapping("/add")
    public Result<String> addForum(@RequestBody Forum forum) {
        // 1. 获取当前登录用户
        Long userId = com.itxiaole.tieba.until.UserHolder.getUser();
        com.itxiaole.tieba.entity.User user = userService.getById(userId);
        
        // 2. 校验经验值 (需要经验 > 10000)
        if (user == null || user.getExperience() == null || user.getExperience() <= 10000) {
            return Result.error("您的经验值不足 10000，无法创建贴吧板块");
        }
        
        // 3. 基础信息补充
        forum.setPostCount(0);
        forum.setCreateTime(java.time.LocalDateTime.now());
        
        boolean save = forumService.save(forum);
        if (save) {
            return Result.success("板块创建成功");
        }
        return Result.error("板块创建失败");
    }

    @GetMapping("/page")
    public Result<Page<Forum>> getPage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String name
            ) {

        Page<Forum> forumPage = forumService.getForumPage(current, size, name);

        return Result.success(forumPage);
    }
}
