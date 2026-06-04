package com.itxiaole.tieba.until;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.io.File;
import java.util.Map;

@Component
@Slf4j
public class MailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Autowired(required = false)
    private FreeMarkerConfigurer freeMarkerConfigurer;

    /**
     * 发送简单文本邮件
     */
    public void sendSimpleMail(String to, String subject, String content) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(content);
        
        try {
            mailSender.send(message);
            log.info("简单邮件发送成功，收件人：{}", to);
        } catch (Exception e) {
            log.error("简单邮件发送失败", e);
            throw new RuntimeException("邮件发送失败", e);
        }
    }

    /**
     * 发送HTML格式邮件
     */
    public void sendHtmlMail(String to, String subject, String htmlContent) {
        MimeMessage message = mailSender.createMimeMessage();
        
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);  // true表示是HTML内容
            
            mailSender.send(message);
            log.info("HTML邮件发送成功，收件人：{}", to);
        } catch (MessagingException e) {
            log.error("HTML邮件发送失败", e);
            throw new RuntimeException("HTML邮件发送失败", e);
        }
    }

    /**
     * 发送带附件的邮件
     */
    public void sendAttachmentMail(String to, String subject, String content, String filePath) {
        MimeMessage message = mailSender.createMimeMessage();
        
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, false);  // false表示纯文本
            
            // 添加附件
            FileSystemResource file = new FileSystemResource(new File(filePath));
            String fileName = file.getFilename();
            helper.addAttachment(fileName, file);
            
            mailSender.send(message);
            log.info("带附件邮件发送成功，收件人：{}", to);
        } catch (MessagingException e) {
            log.error("带附件邮件发送失败", e);
            throw new RuntimeException("带附件邮件发送失败", e);
        }
    }

    /**
     * 发送带内联图片的邮件
     */
    public void sendInlineResourceMail(String to, String subject, String content, String filePath, String contentId) {
        MimeMessage message = mailSender.createMimeMessage();
        
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);  // HTML内容
            
            // 添加内联图片
            FileSystemResource resource = new FileSystemResource(new File(filePath));
            helper.addInline(contentId, resource);
            
            mailSender.send(message);
            log.info("带内联图片邮件发送成功，收件人：{}", to);
        } catch (MessagingException e) {
            log.error("带内联图片邮件发送失败", e);
            throw new RuntimeException("带内联图片邮件发送失败", e);
        }
    }

    /**
     * 发送模板邮件
     */
    public void sendTemplateMail(String to, String subject, String templateName, Map<String, Object> model) {
        try {
            // 使用FreeMarker模板
            String htmlContent = FreeMarkerTemplateUtils.processTemplateIntoString(
                    freeMarkerConfigurer.getConfiguration().getTemplate(templateName),
                    model);
            
            // 发送HTML邮件
            sendHtmlMail(to, subject, htmlContent);
        } catch (Exception e) {
            log.error("模板邮件发送失败", e);
            throw new RuntimeException("模板邮件发送失败", e);
        }
    }
}