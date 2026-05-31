package com.nlsql.nl_sql_engine.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class QueryResponse {
    private String generatedSQL;
    private List<Map<String, Object>> results;
    private boolean fromCache;
    private String error;
}