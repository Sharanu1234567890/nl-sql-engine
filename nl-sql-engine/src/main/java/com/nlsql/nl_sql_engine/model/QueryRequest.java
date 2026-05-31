package com.nlsql.nl_sql_engine.model;


import lombok.Data;

@Data
public class QueryRequest {
    private String question;
    private String role;
}