package com.nlsql.nl_sql_engine.model;


import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "audit_logs")
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String question;
    private String generatedSQL;
    private String role;
    private boolean success;
    private LocalDateTime createdAt;
}