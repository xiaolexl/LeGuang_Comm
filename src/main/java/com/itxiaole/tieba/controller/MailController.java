package com.itxiaole.tieba.controller;

import com.itxiaole.tieba.until.MailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/mail")
public class MailController {

    @Autowired
    private MailService mailService;

    /**
     * 发送简单文本邮件
     */
    @PostMapping("/simple")
    public String sendSimpleMail(
            @RequestParam String to,
            @RequestParam String subject,
            @RequestParam String content) {
        
        mailService.sendSimpleMail(to, subject, content);
        return "简单邮件发送成功";
    }

    /**
     * 发送HTML邮件
     */
    @PostMapping("/html")
    public String sendHtmlMail(
            @RequestParam String to,
            @RequestParam String subject,
            @RequestParam String htmlContent) {
        
        mailService.sendHtmlMail(to, subject, htmlContent);
        return "HTML邮件发送成功";
    }

    /**
     * 发送带附件的邮件
     */
    @PostMapping("/attachment")
    public String sendAttachmentMail(
            @RequestParam String to,
            @RequestParam String subject,
            @RequestParam String content,
            @RequestParam String filePath) {
        
        mailService.sendAttachmentMail(to, subject, content, filePath);
        return "带附件邮件发送成功";
    }

    /**
     * 发送注册确认邮件（使用模板）
     */
    @PostMapping("/register")
    public String sendRegisterMail(
            @RequestParam String to,
            @RequestParam String username,
            @RequestParam String verifyCode) {
        
        Map<String, Object> model = new HashMap<>();
        model.put("username", username);
        model.put("verifyUrl", "http://yourdomain.com/verify?code=" + verifyCode);
        
        mailService.sendTemplateMail(to, "【系统】注册确认邮件", "register.html", model);
        return "注册确认邮件发送成功";
    }

    /**
     * 发送带内联图片的邮件
     */
    @PostMapping("/inline")
    public String sendInlineMail(
            @RequestParam String to,
            @RequestParam String subject,
            @RequestParam String imagePath) {
        
        // HTML内容中引用内联图片
        String htmlContent = "<html><body><h3>这是一封带内联图片的邮件</h3>" +
                           "<img src='cid:image1'></body></html>";
        
        mailService.sendInlineResourceMail(to, subject, htmlContent, imagePath, "image1");
        return "带内联图片邮件发送成功";
    }
}