package com.itxiaole.tieba.Listener;

import com.itxiaole.tieba.config.RabbitMQConfig;
import com.itxiaole.tieba.entity.Post;
import com.itxiaole.tieba.service.IAuditService;
import com.itxiaole.tieba.service.IPostService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component // 👈 第一步：必须加这个注解，告诉 Spring 容器：“你来托管我，我是个组件”
public class SimplePostListener {

    /**
     * 👈 第二步：核心核心！用 @RabbitListener 开启监听
     * queues = RabbitMQConfig.AUDIT_QUEUE ：意思是让这个方法死死盯着你在配置类里声明的那个排队队伍。
     * Map<String, Object> msg ：因为我们之前加了 JSON 转换器，所以这里直接用 Map 就能完美接收发帖的参数！
     */
    @Autowired
    IPostService postService;

    @Autowired
    private IAuditService auditService;

    @RabbitListener(queues = RabbitMQConfig.AUDIT_QUEUE)
    public void receiveMessage(Map<String, Object> msg) {

        Long postId = ((Number) msg.get("postId")).longValue();
        String title = (String) msg.get("title");
        String content = (String) msg.get("content");
        System.out.println("审核开始...");
        String result = auditService.auditPost(title, content);
        // 第三步：把接收到的消息打印出来
        System.out.println("\n=================================================");
        System.out.println("📌 帖子ID   : " + msg.get("postId"));
        System.out.println("📌 帖子标题 : " + msg.get("title"));
        System.out.println("📌 帖子内容 : " + msg.get("content"));
        System.out.println("=================================================\n");
        // 接下来根据 result 是 APPROVED 还是 REJECTED 去做 postService.updateById 修改数据库状态即可

        Post post = postService.getById(postId);
        if (post != null) {
            if ("APPROVED".equals(result)) {
                post.setStatus((byte)1); // 审核通过
                System.out.println("审核通过，帖子已发布！");
            } else if ("REJECTED".equals(result)) {
                post.setStatus((byte)2); // 违规拦截
                System.out.println("违规拦截，帖子已下架！");
            }
            postService.updateById(post);
        }
    }
}