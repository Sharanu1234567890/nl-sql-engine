package com.nlsql.nl_sql_engine.repository;


import com.nlsql.nl_sql_engine.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
}