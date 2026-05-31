package com.nlsql.nl_sql_engine.security;


import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;

@Component
public class TableAccessPolicy {

    private static final Map<String, List<String>> ROLE_TABLE_MAP = Map.of(
            "admin",   List.of("*"),
            "hr",      List.of("employees", "departments", "salaries"),
            "finance", List.of("invoices", "payments", "accounts"),
            "sales",   List.of("orders", "customers", "products"),
            "default", List.of("audit_logs")
    );

    public List<String> getAllowedTables(String role) {
        return ROLE_TABLE_MAP.getOrDefault(role.toLowerCase(), List.of());
    }

    public boolean hasAccess(String role, String table) {
        List<String> allowed = getAllowedTables(role);
        return allowed.contains("*") || allowed.contains(table.toLowerCase());
    }
}