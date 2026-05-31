package com.nlsql.nl_sql_engine.controller;


import com.nlsql.nl_sql_engine.model.QueryRequest;
import com.nlsql.nl_sql_engine.model.QueryResponse;
import com.nlsql.nl_sql_engine.service.*;
import com.nlsql.nl_sql_engine.service.SQLValidatorService.ValidationResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/query")
@RequiredArgsConstructor
public class QueryController {

    private final NLToSQLService nlToSQLService;
    private final SQLValidatorService validatorService;
    private final QueryExecutorService executorService;
    private final SelfHealingService selfHealingService;
    private final CacheService cacheService;
    private final AuditService auditService;

    @PostMapping
    public ResponseEntity<QueryResponse> query(@RequestBody QueryRequest request) {

        String question = request.getQuestion();
        String role = request.getRole() != null ? request.getRole() : "default";

        // 1. Check cache
        Object cached = cacheService.get(question);
        if (cached != null) {
            return ResponseEntity.ok(
                    new QueryResponse(null, (List<Map<String, Object>>) cached, true, null)
            );
        }

        // 2. Generate SQL
        String sql = nlToSQLService.generateSQL(question, role);

        // 3. Validate
        ValidationResult validation = validatorService.validate(sql);
        if (!validation.valid()) {
            auditService.log(question, sql, role, false);
            return ResponseEntity.badRequest().body(
                    new QueryResponse(sql, null, false, validation.reason())
            );
        }

        // 4. Execute with self healing
        List<Map<String, Object>> results;
        try {
            results = executorService.execute(sql);
        } catch (Exception e) {
            // Self heal
            String fixedSQL = selfHealingService.fixSQL(sql, e.getMessage());
            ValidationResult fixedValidation = validatorService.validate(fixedSQL);
            if (!fixedValidation.valid()) {
                auditService.log(question, fixedSQL, role, false);
                return ResponseEntity.badRequest().body(
                        new QueryResponse(fixedSQL, null, false, "Self healing failed: " + fixedValidation.reason())
                );
            }
            try {
                results = executorService.execute(fixedSQL);
                sql = fixedSQL;
            } catch (Exception ex) {
                auditService.log(question, fixedSQL, role, false);
                return ResponseEntity.internalServerError().body(
                        new QueryResponse(fixedSQL, null, false, "Query failed: " + ex.getMessage())
                );
            }
        }

        // 5. Cache + audit
        cacheService.save(question, results);
        auditService.log(question, sql, role, true);

        return ResponseEntity.ok(
                new QueryResponse(sql, results, false, null)
        );
    }
}