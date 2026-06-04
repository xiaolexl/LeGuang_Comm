package com.itxiaole.tieba.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itxiaole.tieba.entity.Forum;
import com.itxiaole.tieba.mapper.ForumMapper;
import com.itxiaole.tieba.service.IForumService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 贴吧（板块）表 服务实现类
 * </p>
 *
 * @author xiaole
 * @since 2026-06-02
 */
@Service
public class ForumServiceImpl extends ServiceImpl<ForumMapper, Forum> implements IForumService {
    @Override
    public Page<Forum> getForumPage(Integer current, Integer size, String name) {
        // 1. 初始化 MyBatis-Plus 分页构造器
        Page<Forum> pageInfo = new Page<>(current, size);

        // 2. 构建条件查询器
        LambdaQueryWrapper<Forum> wrapper = new LambdaQueryWrapper<>();

        // 动态判断：如果前端传了 name 参数，就拼装 LIKE 模糊查询
        if (name != null && !name.trim().isEmpty()) {
            wrapper.like(Forum::getName, name); // WHERE name LIKE %Java%
        }

        // 3. 排序策略：优先按照帖子数量倒序，如果数量一样，再按创建时间倒序
        wrapper.orderByDesc(Forum::getPostCount)
                .orderByDesc(Forum::getCreateTime);

        // 4. 调用父类 BaseMapper 的 selectPage 方法执行查询
        // MyBatis-Plus 拦截器会自动在这条语句后面加上 LIMIT 并在前面执行一条 COUNT
        return this.baseMapper.selectPage(pageInfo, wrapper);
    }
}
