package com.nlsql.nl_sql_engine.service;


import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class QueryExecutorService {

    private final JdbcTemplate jdbcTemplate;

    public List<Map<String, Object>> execute(String sql) {
        return jdbcTemplate.queryForList(sql);
    }
}