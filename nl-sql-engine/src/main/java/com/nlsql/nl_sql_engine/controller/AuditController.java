package com.nlsql.nl_sql_engine.controller;


import com.nlsql.nl_sql_engine.model.AuditLog;
import com.nlsql.nl_sql_engine.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditLogRepository auditLogRepository;

    @GetMapping
    public List<AuditLog> getAll() {
        return auditLogRepository.findAll();
    }
}