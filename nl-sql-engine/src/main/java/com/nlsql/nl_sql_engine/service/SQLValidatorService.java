package com.nlsql.nl_sql_engine.service;


import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class SQLValidatorService {

    private static final List<String> DANGEROUS_KEYWORDS = List.of(
            "DROP", "DELETE", "TRUNCATE",
            "UPDATE", "INSERT", "ALTER",
            "CREATE", "EXEC", "EXECUTE",
            "GRANT", "REVOKE"
    );

    public ValidationResult validate(String sql) {
        if (sql == null || sql.isBlank()) {
            return ValidationResult.fail("SQL is empty");
        }

        String upper = sql.toUpperCase().trim();

        if (!upper.startsWith("SELECT")) {
            return ValidationResult.fail("Only SELECT queries allowed");
        }

        for (String keyword : DANGEROUS_KEYWORDS) {
            if (upper.contains(keyword)) {
                return ValidationResult.fail("Dangerous keyword detected: " + keyword);
            }
        }

        if (upper.contains("--") || upper.contains(";")) {
            return ValidationResult.fail("Suspicious pattern detected");
        }

        return ValidationResult.pass();
    }

    public record ValidationResult(boolean valid, String reason) {
        public static ValidationResult pass() {
            return new ValidationResult(true, null);
        }
        public static ValidationResult fail(String reason) {
            return new ValidationResult(false, reason);
        }
    }
}