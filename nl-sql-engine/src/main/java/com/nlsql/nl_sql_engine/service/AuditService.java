package com.nlsql.nl_sql_engine.service;


import com.nlsql.nl_sql_engine.model.AuditLog;
import com.nlsql.nl_sql_engine.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public void log(String question, String sql, String role, boolean success) {
        AuditLog log = new AuditLog();
        log.setQuestion(question);
        log.setGeneratedSQL(sql);
        log.setRole(role);
        log.setSuccess(success);
        log.setCreatedAt(LocalDateTime.now());
        auditLogRepository.save(log);
    }
}