package com.nlsql.nl_sql_engine.service;


import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SelfHealingService {

    private final ChatClient chatClient;

    public String fixSQL(String failedSQL, String errorMessage) {
        String prompt = """
                This PostgreSQL query failed:
                %s
                
                Error:
                %s
                
                Fix the query. Return ONLY the corrected SQL. No explanation. No markdown.
                """.formatted(failedSQL, errorMessage);

        return chatClient.prompt()
                .user(prompt)
                .call()
                .content()
                .trim();
    }
}