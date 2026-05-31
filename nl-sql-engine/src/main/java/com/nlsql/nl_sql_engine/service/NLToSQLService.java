package com.nlsql.nl_sql_engine.service;

import com.nlsql.nl_sql_engine.security.RoleBasedSchemaFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NLToSQLService {

    private final ChatClient chatClient;
    private final RoleBasedSchemaFilter schemaFilter;

    public String generateSQL(String question, String role) {
        String schema = schemaFilter.getFilteredSchema(role);

        if (schema.isBlank()) {
            return "ACCESS_DENIED";
        }

        String prompt = """
                You are a PostgreSQL expert.
                Given the database schema below, write a SQL query for the user's question.
                
                STRICT RULES:
                - Return ONLY the SQL query
                - No markdown, no explanation, no backticks
                - Only SELECT statements
                - Must be valid PostgreSQL syntax
                
                DATABASE SCHEMA:
                %s
                
                USER QUESTION: %s
                
                SQL:
                """.formatted(schema, question);

        return chatClient.prompt()
                .user(prompt)
                .call()
                .content()
                .trim();
    }
}
