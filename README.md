
 # NL-SQL Engine                     

> Convert plain English to SQL — powered by Spring AI and Groq LLaMA 3.3 70B

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen)](https://spring.io/projects/spring-boot)
[![Spring AI](https://img.shields.io/badge/Spring%20AI-1.0.0--M6-blue)](https://spring.io/projects/spring-ai)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue)](https://www.postgresql.org/)
[![Redis](https://img.shields.io/badge/Redis-7-red)](https://redis.io/)
[![Java](https://img.shields.io/badge/Java-17-orange)](https://www.java.com/)
[![License](https://img.shields.io/badge/License-MIT-green)](LICENSE)
 
---

## What is this?

NL-SQL Engine lets anyone query a database using plain English — no SQL knowledge required.

Type **"show me the top 5 employees by salary"** and the system:
1. Reads your database schema automatically
2. Generates the correct SQL using an LLM
3. Validates the SQL for security
4. Executes it on your PostgreSQL database
5. Returns the results — or auto-fixes the query if it fails

The same concept powers **Swiggy's internal Hermes tool** used by their data and engineering teams. This is the Java implementation built with Spring AI.

---

## Live Demo

```
POST /api/query
{
  "question": "show all employees with salary above 50000",
  "role": "hr"
}
```

```json
{
  "generatedSQL": "SELECT * FROM employees WHERE salary > 50000",
  "results": [
    { "id": 1, "name": "Rahul Sharma", "salary": 95000, "department": "Engineering" },
    { "id": 3, "name": "Amit Kumar",  "salary": 120000, "department": "Finance" }
  ],
  "fromCache": false,
  "error": null
}
```

---

## Architecture

```
User Request (question + role)
         │
         ▼
┌─────────────────────────────────────────┐
│           QueryController               │
│      Orchestrates entire pipeline       │
└──────────────────┬──────────────────────┘
                   │
         ┌─────────▼──────────┐
         │  Redis Cache Check  │ ──── Cache hit → return instantly
         └─────────┬───────────┘
                   │ Cache miss
         ┌─────────▼────────────────┐
         │  RoleBasedSchemaFilter   │
         │  Read information_schema │
         │  Filter by user role     │
         └─────────┬────────────────┘
                   │
         ┌─────────▼──────────┐
         │   NLToSQLService   │
         │  Schema + Question │
         │  → Spring AI       │
         │  → Groq LLaMA 3.3  │
         │  → SQL string      │
         └─────────┬──────────┘
                   │
         ┌─────────▼───────────────┐
         │   SQLValidatorService   │
         │  Block DROP DELETE etc  │
         │  Block SQL injection    │
         └─────────┬───────────────┘
                   │
         ┌─────────▼────────────────┐
         │  QueryExecutorService    │
         │  JdbcTemplate → Postgres │
         └────┬──────────┬──────────┘
         Success       Failure
              │              │
              │    ┌─────────▼──────────┐
              │    │  SelfHealingService │
              │    │  Error → LLM Fix   │
              │    │  Validate → Retry  │
              │    └─────────┬──────────┘
              │              │
         ┌────▼──────────────▼────┐
         │   Cache + Audit Log    │
         │  Redis save 10min TTL  │
         │  Log to audit_logs DB  │
         └────────────┬───────────┘
                      │
                   Response
```

---

## Features

### Core
- **Natural Language to SQL** — type plain English, get real data
- **Spring AI Integration** — clean abstraction over Groq LLaMA 3.3 70B
- **Auto Schema Detection** — reads your DB structure at runtime from `information_schema`
- **Self-Healing Queries** — if SQL fails, LLM auto-fixes and retries
- **Redis Caching** — same question twice returns in milliseconds from cache
- **Full Audit Trail** — every query logged with role, SQL, success, timestamp

### Security
- **Role-Based Schema Filter** — HR role never sees Finance tables in the LLM prompt
- **SQL Validator** — blocks `DROP`, `DELETE`, `UPDATE`, `INSERT`, `ALTER`, `TRUNCATE`
- **SQL Injection Protection** — blocks `--` comments and `;` chaining
- **Information Control** — LLM cannot generate SQL for tables it never knew existed

### Export & Observability
- **Excel Export** — download any query result as a professional `.xlsx` file
- **Audit Controller** — `GET /api/audit` returns full query history
- **Role Access Matrix** — configurable per-role table permissions

---

## Role Access Matrix

| Role    | Accessible Tables                          |
|---------|--------------------------------------------|
| admin   | All tables                                 |
| hr      | employees, departments, salaries           |
| finance | invoices, payments, accounts               |
| sales   | orders, customers, products                |
| default | audit_logs only                            |

---

## Tech Stack

| Layer          | Technology              |
|----------------|-------------------------|
| Language       | Java 17                 |
| Framework      | Spring Boot 3.2.5       |
| AI Framework   | Spring AI 1.0.0-M6      |
| LLM Provider   | Groq                    |
| LLM Model      | LLaMA 3.3 70B Versatile |
| Database       | PostgreSQL 15           |
| Cache          | Redis 7                 |
| ORM            | Spring Data JPA         |
| Build Tool     | Maven                   |
| Export         | Apache POI 5.2.5        |
| Containerization | Docker Compose        |

---

## Project Structure

```
nl-sql-engine/
├── src/main/java/com/nlsql/
│   ├── NlSqlEngineApplication.java
│   ├── config/
│   │   ├── RedisConfig.java          # JSON serialization for Redis
│   │   └── SpringAIConfig.java       # ChatClient bean — Groq integration
│   ├── controller/
│   │   ├── QueryController.java      # Main API + Excel export
│   │   └── AuditController.java      # Query history endpoint
│   ├── service/
│   │   ├── NLToSQLService.java       # LLM prompt + SQL generation
│   │   ├── SQLValidatorService.java  # Security validation
│   │   ├── QueryExecutorService.java # DB execution
│   │   ├── SelfHealingService.java   # Auto-fix failed queries
│   │   ├── CacheService.java         # Redis read/write
│   │   ├── AuditService.java         # Query logging
│   │   └── ExcelExportService.java   # .xlsx generation
│   ├── security/
│   │   ├── RoleBasedSchemaFilter.java # Schema filtering per role
│   │   └── TableAccessPolicy.java    # Role → table mapping
│   ├── model/
│   │   ├── QueryRequest.java
│   │   ├── QueryResponse.java
│   │   └── AuditLog.java
│   └── repository/
│       └── AuditLogRepository.java
├── src/main/resources/
│   ├── application.yml
│   └── static/
│       └── index.html                # Frontend UI
├── docker-compose.yml
└── pom.xml
```

---

## Getting Started

### Prerequisites

- Java 17+
- Docker Desktop
- Maven 3.6+
- Free Groq API key — [console.groq.com](https://console.groq.com)

### Step 1 — Clone the repo

```bash
git clone https://github.com/Sharanu1234567890/nl-sql-engine.git
cd nl-sql-engine
```

### Step 2 — Start PostgreSQL and Redis

```bash
docker-compose up -d
```

### Step 3 — Configure your Groq API key

Open `src/main/resources/application.yml` and set:

```yaml
spring:
  ai:
    openai:
      api-key: your-groq-api-key-here
      base-url: https://api.groq.com/openai
      chat:
        options:
          model: llama-3.3-70b-versatile
          temperature: 0.0
```

### Step 4 — Run the application

```bash
mvn spring-boot:run
```

### Step 5 — Open the UI

```
http://localhost:8080
```

---

## API Reference

### Run a natural language query

```http
POST /api/query
Content-Type: application/json

{
  "question": "show top 3 highest paid employees",
  "role": "admin"
}
```

**Response:**

```json
{
  "generatedSQL": "SELECT * FROM employees ORDER BY salary DESC LIMIT 3",
  "results": [...],
  "fromCache": false,
  "error": null
}
```

### Export query results to Excel

```http
POST /api/query/export
Content-Type: application/json

{
  "question": "count employees by department",
  "role": "hr"
}
```

Returns a downloadable `.xlsx` file with two sheets — Results and Generated SQL.

### Get all audit logs

```http
GET /api/audit
```

---

## How the LLM Knows Your Schema

No training. No fine-tuning.

At runtime, we query PostgreSQL's built-in `information_schema`:

```sql
SELECT column_name, data_type
FROM information_schema.columns
WHERE table_schema = 'public'
AND table_name = 'employees'
```

This gives us every table name and column definition. We convert it to plain text and inject it into the LLM prompt on every request. Fresh. Accurate. Zero maintenance.

When a new table is added to your database — the next request automatically includes it.

---

## How Self-Healing Works

```
Generated SQL fails on PostgreSQL
           │
           ▼
Capture exact error message
           │
           ▼
Send to LLM:
"This SQL failed: SELECT * FROM employe WHERE salary > 50000
 Error: relation 'employe' does not exist
 Fix it. Return only corrected SQL."
           │
           ▼
LLM returns: SELECT * FROM employees WHERE salary > 50000
           │
           ▼
Run through SQL Validator again
           │
           ▼
Execute fixed SQL
           │
           ▼
User receives correct results
```

One retry. No infinite loop. If retry also fails — clean error returned.

---

## Load Test Data

Connect to PostgreSQL and insert sample data:

```bash
docker exec -it nlsql-postgres psql -U postgres -d nlsqldb
```

```sql
CREATE TABLE employees (
  id SERIAL PRIMARY KEY,
  name VARCHAR(100),
  department VARCHAR(100),
  salary NUMERIC,
  created_at TIMESTAMP DEFAULT now()
);

INSERT INTO employees (name, department, salary) VALUES
('Rahul Sharma',  'Engineering', 95000),
('Priya Patel',   'HR',          75000),
('Amit Kumar',    'Finance',     120000),
('Sneha Reddy',   'Engineering', 85000),
('Vikram Singh',  'Sales',       60000),
('Neha Joshi',    'HR',          45000),
('Arjun Mehta',   'Finance',     150000),
('Divya Nair',    'Engineering', 110000);
```

---

## Example Queries to Try

| Question | Role |
|----------|------|
| show all employees with salary above 80000 | admin |
| count employees by department | admin |
| show top 3 highest paid employees | admin |
| show all employees in HR | hr |
| show all audit logs | admin |
| show failed queries from audit logs | admin |

---

## Known Limitations

| Issue | Current behaviour | Production fix |
|-------|------------------|----------------|
| Large schemas | All tables sent to LLM | RAG with pgvector — V2 |
| Complex joins | May fail on 4+ tables | Few-shot examples — V2 |
| Column DROP false positive | `drop_date` column blocked | JSQLParser word boundary check |
| Async audit | Audit failure blocks response | `@Async` annotation — V2 |
| Role storage | Hardcoded in Java | DB table `role_permissions` — V2 |

---

## V2 Roadmap

- **RAG schema retrieval** — embed schema in pgvector, fetch only top 3 relevant tables via cosine similarity (same as Swiggy Hermes V2 — improves accuracy 60% → 90%)
- **Few-shot examples** — store past successful queries as embeddings, inject into prompt
- **JWT authentication** — real user management
- **Column-level permissions** — HR sees name but not salary
- **Multi-database support** — MySQL, MongoDB, Snowflake
- **LLM router** — Groq for simple queries, OpenAI GPT-4 for complex
- **Async audit logging** — `@Async` so audit failure never blocks user
- **Streaming results via SSE** — for large result sets
- **Kubernetes deployment** — 3 replicas with horizontal scaling

---

## Real World Reference

Swiggy built the same concept internally — they call it **Hermes**. It allows engineers, business teams, and product managers to query their data warehouse in plain English. Thousands of queries per day.

This project follows the same core architecture. V2 roadmap implements the same improvements Swiggy made in Hermes V2 — RAG-based schema retrieval and few-shot examples.

---

## Contributing

Pull requests welcome. For major changes please open an issue first.

---

## License

MIT License — see [LICENSE](LICENSE) for details.

---

## Author

**Sharanu Malipatil**
IIT Student | Spring Boot + AI Backend Engineer
[github.com/Sharanu1234567890](https://github.com/Sharanu1234567890)
