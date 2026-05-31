package com.nlsql.nl_sql_engine.security;


import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class RoleBasedSchemaFilter {

    private final JdbcTemplate jdbcTemplate;
    private final TableAccessPolicy accessPolicy;

    public String getFilteredSchema(String role) {
        List<String> allowedTables = getAllowedTablesForRole(role);
        StringBuilder schema = new StringBuilder();

        for (String table : allowedTables) {
            schema.append("Table: ").append(table).append("\nColumns:\n");

            List<Map<String, Object>> columns = jdbcTemplate.queryForList(
                    "SELECT column_name, data_type " +
                            "FROM information_schema.columns " +
                            "WHERE table_schema = 'public' AND table_name = ? " +
                            "ORDER BY ordinal_position",
                    table
            );

            for (Map<String, Object> col : columns) {
                schema.append("  - ")
                        .append(col.get("column_name"))
                        .append(" (")
                        .append(col.get("data_type"))
                        .append(")\n");
            }
            schema.append("\n");
        }
        return schema.toString();
    }

    private List<String> getAllowedTablesForRole(String role) {
        List<String> allowed = accessPolicy.getAllowedTables(role);
        if (allowed.contains("*")) {
            return jdbcTemplate.queryForList(
                    "SELECT table_name FROM information_schema.tables " +
                            "WHERE table_schema = 'public' AND table_type = 'BASE TABLE'",
                    String.class
            );
        }
        return allowed;
    }
}