package com.itxiaole.tieba.service.impl;

import com.itxiaole.tieba.service.IAuditService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuditServiceImpl implements IAuditService {

    @Autowired
    private ChatClient deepSeekChatClient; // 👈 注入我们在配置类里定义好的客户端

    @Override
    public String auditPost(String title, String content) {
        // 1. 设计极其严密、专业的结构化 Prompt
        String prompt = """
                你是一个网络论坛社区的专业内容审核专家。请对以下提交的帖子内容进行合规性审查。
                
                【帖子标题】: %s
                【帖子内容】: %s
                
                审查要求：
                1. 检查是否包含敏感政治、辱骂仇恨、暴力血腥、色情低俗，或者垃圾广告引流内容。
                2. 你必须且只能返回以下两个英文单词之一，绝对不要带有任何标点符号、解释或多余的汉字：
                   - APPROVED : 代表内容完全健康合规，审核通过。
                   - REJECTED : 代表发现违规或不适宜内容，审核不通过。
                """.formatted(title, content);

        try {
            // 2. 使用 Spring AI 链式调用给 DeepSeek 发送请求
            String aiResponse = deepSeekChatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            // 3. 对大模型的返回进行严格的数据清洗（去空格、去单双引号、统一转大写）
            if (aiResponse == null) {
                return "ERROR";
            }
            return aiResponse.trim().replaceAll("[\"']", "").toUpperCase();

        } catch (Exception e) {
            // 4. 健壮性防抖：如果网络波动、超时或 API 余额不足报错，打印错误并返回 ERROR，不干扰主业务
            System.err.println("💥 【DeepSeek API 异常】调用大模型审查失败，原因: " + e.getMessage());
            return "ERROR";
        }
    }
}