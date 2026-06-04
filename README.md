# 乐光社区 (Tieba) 全栈项目技术开发手册

## 1. 项目定位与核心价值

乐光社区是一个基于 **Spring Boot 4.0.6** 与 **Vue 3** 构建的现代化论坛系统。项目不仅实现了贴吧的核心交互（发帖、回帖、点赞、等级系统），更深度集成了 **DeepSeek 大模型** 实现内容的自动化、智能化审核，解决了传统社区人工审核压力大、关键词过滤不准确的痛点。

---

## 2. 核心技术架构

### 2.1 后端技术栈 (Tieba Backend)

- **核心框架**: Spring Boot 4.0.6, Java 17
- **持久层**: MyBatis Plus 3.5.15 + MySQL 8.0
- **缓存层**: Redis 7.0 (存储分布式 Session、验证码、点赞缓存、防重放)
- **消息层**: RabbitMQ (处理后台异步任务及数据同步)
- **AI 审计**: Spring AI + DeepSeek API (提供语义级内容合规检测)
- **安全层**: JWT + 自定义 HandlerInterceptor (双重校验：Token 合法性 + Redis 状态校验)
- **中间件**: 阿里云 OSS (云端资源存储), Spring Mail (SMTP 163 服务)

### 2.2 前端技术栈 (Tieba-Vue Frontend)

- **核心框架**: Vue 3.5 (Composition API)
- **构建工具**: Vite 8.0 (已优化 ngrok 穿透支持)
- **状态管理**: Pinia
- **路由**: Vue Router 5
- **UI 组件**: Element Plus
- **通信**: Axios (集成全局响应异常拦截)

---

## 3. 核心业务流程解析

### 3.1 智能化发帖与拦截 (AI Content Shield)

1. **用户发布**: 前端提交帖子标题与内容。
2. **同步审计**: 后端 `PostController` 调用 `AuditService`。
3. **语义分析**: DeepSeek 对内容进行政治、色情、暴力、广告等全维度扫描。
4. **决策返回**: 
   - `APPROVED`: 状态设为 1，帖子立即全网可见。
   - `REJECTED`: 接口返回 400 状态码及错误信息，拦截保存。
   - `ERROR`: 系统异常时，状态设为 0，存入数据库待人工审核，保证业务不中断。

### 3.2 严密的账户安全体系

1. **注册流**: 邮箱输入 -> 发送 163 验证码 -> Redis 存入 `register:code:{email}` -> 验证提交 -> 密码加密入库。
2. **登录流**: 用户登录 -> 生成 JWT -> 存入 Redis `login:token:{userId}` -> 返回 Token 供前端 `localStorage` 存储。
3. **鉴权流**: 拦截器 `LoginInterceptor` 拦截 `/**`，校验 Token 签名及其在 Redis 中是否存在（支持强制下线/单点登录扩展）。

### 3.3 高性能交互设计

- **点赞系统**: 使用 Redis 的 `Set` 集合实现。Key 为 `post:liked:{postId}`，存储所有点赞用户的 ID。
  - **优势**: 保证点赞幂等性（一个用户只能点一次），点赞/取消点赞均为 O(1) 复杂度。
- **经验值系统**: 通过用户活跃度（发帖、获赞）累积经验值，解锁资深用户特权（如创建贴吧）。

---

## 4. 数据库结构 (Schema)

### 4.1 用户表 (t_user)

- `id`: 用户主键 (Long, AUTO_INCREMENT)
- `username`: 唯一账号
- `password`: 加密密码
- `user_email`: 绑定邮箱
- `experience`: 经验值
- `create_time`/`update_time`: 时间审计

### 4.2 帖子表 (t_post)

- `id`: 帖子主键
- `forum_id`: 所属贴吧
- `user_id`: 作者
- `title`/`content`: 标题内容
- `status`: 0-待审, 1-通过, 2-违规
- `view_count`/`like_count`: 阅读与点赞数

### 4.3 评论表 (t_comment)

- `parent_id`: 父评论 ID (0 代表一级评论)
- `post_id`: 关联帖子
- `content`: 评论文本

---

## 5. 关键配置文件指南

### 5.1 后端 application.yml

```yaml
jwt:
  secret-key: # 必须保持高度机密
  expire-time: 7200000 # 2小时过期
  token-name: token

spring:
  ai:
    deepseek:
      api-key: # 注册 DeepSeek 获取的 SK
  mail:
    host: smtp.163.com
    password: # 授权码而非登录密码
```

### 5.2 前端 vite.config.ts (内网穿透优化)

```typescript
server: {
  host: '0.0.0.0', // 监听局域网
  allowedHosts: true, // 解决 ngrok 域名拦截
  proxy: {
    '/api': { target: 'http://localhost:8080' } // 自动解决跨域
  }
}
```

---

## 6. 环境搭建与部署

1. **基础环境**: 安装 JDK 17, MySQL 8.0, Redis 7, RabbitMQ 3.x。

2. **数据库初始化**: 执行 SQL 脚本创建 `community_db` 数据库及对应表。

3. **后端配置**: 修改 `application.yml` 中的数据库账号密码、DeepSeek Key、邮件授权码。

4. **后端运行**: `mvn clean install` 后启动 `TiebaApplication`。

5. **前端运行**: 

   ```bash
   npm install
   npm run dev
   ```

6. **公网访问**: 运行 `ngrok http 5173` 即可获得公网访问链接。

---

**项目维护说明**:

- 本项目采用了全局异常处理机制 `GlobalExceptionHandler`，所有业务逻辑错误会以 `Result.error` 形式返回。
- 业务扩展时，DTO 类如 `UserDTO` 必须配合 `@TableField(exist = false)` 区分业务字段与数据库字段。

**最后更新**: 2026-06-04
**状态**: 核心功能已跑通，支持 AI 实时拦截。
