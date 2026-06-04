package com.itxiaole.tieba.entity.DTO;

import lombok.Data;
import java.io.Serializable;

@Data
public class MailDTO implements Serializable {
    private String recipient;  // 收件人邮箱
    private String[] recipients;  // 多个收件人（可选）
    private String subject;  // 邮件主题
    private String content;  // 邮件内容
    private String templateName;  // 模板名称（可选）
    private Object templateModel;  // 模板数据模型（可选）
}