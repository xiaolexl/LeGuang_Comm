package com.itxiaole.tieba.service;

public interface IAuditService {
    
    /**
     * 智能化合规审查接口
     * @param title 帖子标题
     * @param content 帖子内容
     * @return APPROVED(通过) 或 REJECTED(违规拦截)
     */
    String auditPost(String title, String content);
}