package com.nlsql.nl_sql_engine.controller;

import com.nlsql.nl_sql_engine.model.QueryRequest;
import com.nlsql.nl_sql_engine.model.QueryResponse;
import com.nlsql.nl_sql_engine.service.*;
import com.nlsql.nl_sql_engine.service.SQLValidatorService.ValidationResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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
    private final ExcelExportService excelExportService;

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

        // 3. Access denied check
        if (sql.equals("ACCESS_DENIED")) {
            return ResponseEntity.status(403).body(
                    new QueryResponse(null, null, false, "Access denied for role: " + role)
            );
        }

        // 4. Validate
        ValidationResult validation = validatorService.validate(sql);
        if (!validation.valid()) {
            auditService.log(question, sql, role, false);
            return ResponseEntity.badRequest().body(
                    new QueryResponse(sql, null, false, validation.reason())
            );
        }

        // 5. Execute with self healing
        List<Map<String, Object>> results;
        try {
            results = executorService.execute(sql);
        } catch (Exception e) {
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

        // 6. Cache + audit
        cacheService.save(question, results);
        auditService.log(question, sql, role, true);

        return ResponseEntity.ok(
                new QueryResponse(sql, results, false, null)
        );
    }

    @PostMapping("/export")
    public ResponseEntity<byte[]> exportToExcel(@RequestBody QueryRequest request) {

        String question = request.getQuestion();
        String role = request.getRole() != null ? request.getRole() : "default";

        List<Map<String, Object>> results;
        String sql;

        // 1. Check cache
        Object cached = cacheService.get(question);
        if (cached != null) {
            results = (List<Map<String, Object>>) cached;
            sql = "FROM CACHE";
        } else {
            // 2. Generate SQL
            sql = nlToSQLService.generateSQL(question, role);

            // 3. Access check
            if (sql.equals("ACCESS_DENIED")) {
                return ResponseEntity.status(403).build();
            }

            // 4. Validate
            ValidationResult validation = validatorService.validate(sql);
            if (!validation.valid()) {
                return ResponseEntity.badRequest().build();
            }

            // 5. Execute
            try {
                results = executorService.execute(sql);
            } catch (Exception e) {
                String fixedSQL = selfHealingService.fixSQL(sql, e.getMessage());
                try {
                    results = executorService.execute(fixedSQL);
                    sql = fixedSQL;
                } catch (Exception ex) {
                    return ResponseEntity.internalServerError().build();
                }
            }

            cacheService.save(question, results);
            auditService.log(question, sql, role, true);
        }

        // 6. Export to Excel
        try {
            byte[] excelBytes = excelExportService.exportToExcel(results, sql);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            ));
            headers.setContentDispositionFormData("attachment", "query-results.xlsx");
            return ResponseEntity.ok().headers(headers).body(excelBytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}