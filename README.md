# SkillForge: A Career Readiness & Skill-Gap Analysis Platform

SkillForge is a full-stack career readiness evaluation, skill-gap analysis, and placement intelligence platform built for final-year university students and college placement centers. Instead of providing generic "skills to learn" checklists, SkillForge computes an authoritative, weighted readiness percentage against industry-standard role benchmarks, performs a two-tier gap analysis (Core vs. Good-to-Have competencies), generates an actionable gap-driven learning roadmap, parses resumes automatically via Apache Tika, and equips placement officers with cohort analytics, department heatmaps, and recruiter rosters.

---

## 1. System Architecture & Tech Stack

- **Backend Framework:** Java 17, Spring Boot 3.3.4, Maven
- **Document & Resume Parser:** Apache Tika 2.9.2 (Parses `.pdf`, `.docx`, `.doc`, `.txt`)
- **Security & Token Rotation:** Spring Security 6 with JJWT (0.12.6), BCrypt hashing, dual-token rotation (`SF_AUTH_TOKEN` access token + `SF_REFRESH_TOKEN` refresh token) with replay attack / token reuse detection
- **Database & Persistence:** Spring Data JPA, Hibernate ORM, MySQL 8 (`skillforge2_db`)
- **Frontend Architecture:**
  - **React Single-Page Application (Vite on port 5173):** Modern React, responsive state management, drag & drop resume parser zone with preview confirmation modal, interactive department vs. role heatmap matrix, and candidate CSV export.
  - **Thymeleaf Multi-Page Application (Spring Boot on port 8080):** Server-side rendered views styled with custom editorial CSS.
- **Design Philosophy:** Editorial design tokens (Warm off-white `#F7F4EE`, ink `#1C1B19`, stone border `#D9D4C9`, forest green accent `#1F4D3A`, burnt orange signal `#C2561F`, Fraunces serif display, and IBM Plex Sans body typography).

---

## 2. Core Feature Modules

### Module 1: AI-Powered PDF/Word Resume Parser
- **File Upload:** Drag-and-drop zone in the Profile Builder supporting PDF (`.pdf`) and Microsoft Word (`.docx`) files up to 15MB.
- **Apache Tika Extraction:** Extracts unstructured text from resumes and tokenizes keywords against the master skills catalog using boundary-safe regex (handles symbols like `C++`, `C#`, `.NET`, `Node.js`, `Spring Boot`).
- **Context-Aware Proficiency Estimation:** Analyzes context windows around matched skills to estimate proficiency:
  - **ADVANCED:** Triggered by terms like `expert`, `architect`, `lead`, `senior`, `production`, `scalable`, `5+ years`.
  - **INTERMEDIATE:** Triggered by terms like `developed`, `built`, `proficient`, `maintained`, `hands-on`, `2+ years`.
  - **BEGINNER:** Default baseline for passing mentions.
- **Interactive Preview & Confirmation Modal:** Shows file metadata, confidence metrics, extracted context snippets, and editable proficiency dropdowns prior to applying to the profile.
- **Dynamic Recalculation:** Merging parsed competencies dynamically updates the candidate's career readiness score and roadmap sequence.

### Module 2: Placement Admin Batch Analytics & Readiness Heatmap
- **Executive Metric Cards:** Real-time visibility into Batch Average Score, Placement-Ready Candidate Count & % ($\ge 70\%$), Critical Core Gaps Count, and Total Candidates.
- **Department vs. Career Role Heatmap Matrix:** Visual matrix crossing academic branches (e.g., Computer Science, Information Technology, Electronics, Data Science) with benchmark career roles, color-coded by readiness density:
  - **$\ge 70\%$ Job Ready:** Forest green tile.
  - **$45\text{--}69\%$ Developing:** Burnt orange tile.
  - **$< 45\%$ Early Stage:** Muted stone tile.
- **Competency Deficit Distribution:** Category-level breakdown (`LANGUAGE`, `FRAMEWORK`, `DATABASE`, `TOOLS_AND_PRACTICES`) highlighting where faculty intervention is required.
- **Filterable Candidate Roster & Recruiter CSV Export:** Real-time role and minimum score filters with a one-click button exporting candidate rosters formatted to RFC 4180 CSV specifications.

### Module 3: Secure JWT Refresh Token Rotation
- **Dual Token Architecture:** Short-lived access token (15 mins) + long-lived refresh token (7 days) stored in the database with user mapping and expiration indexes.
- **Secure Cookie Storage:** Access tokens stored via `SF_AUTH_TOKEN` (`HttpOnly`, `SameSite=Lax`), and refresh tokens stored via `SF_REFRESH_TOKEN` (`HttpOnly`, `SameSite=Strict`).
- **Token Reuse & Breach Detection:** If an already-rotated or revoked token is presented, the system flags a replay attack, immediately revokes all active sessions for that user account, and returns HTTP `403 Forbidden`.
- **Frontend Interceptor:** Transparently handles HTTP `401 Unauthorized` responses by invoking `/api/auth/refresh` and re-executing original requests without interrupting user workflow.

### Module 4: Generative AI Tech Stack Roadmap Architect
- **Non-Static Generative Curriculum Architect:** Accepts any desired technology combinations, frameworks, or languages (e.g., `Next.js 15 + FastAPI + PostgreSQL`, `Go Microservices + Kafka + Kubernetes`, `Rust + Tokio + Axum + WebAssembly`).
- **Google Gemini 1.5 & Adaptive Reasoning Engine:** Integrates directly with Google Gemini REST API (`gemini-1.5-flash`) with structured JSON schema outputs. If external keys are unset or rate-limited, the built-in **Adaptive Cognitive Architecture Engine** dynamically decomposes the stack into runtime, web, persistence, and cloud layers.
- **Competency-Aware Context Injection:** Cross-references the student's acquired SkillForge profile skills, explicitly bypassing redundant introductory concepts in Phase 1 and tailoring the roadmap to bridge genuine gaps.
- **Dynamic AI Reasoning & Architecture Blueprint:** Outputs comprehensive reasoning notes, system architecture blueprints, and sequenced multi-phase timelines calibrated to the student's timeframe (4 to 24 weeks) and experience level.
- **Interactive Hands-On Milestones:** Each milestone features estimated hours, key topics, official resource references, and concrete code deliverables.
- **Production Capstone Specifications:** Generates 3 enterprise-ready capstone project proposals designed for GitHub portfolio showcases.
- **Persistent Progress Tracking:** Students can save custom AI roadmaps into their library (`custom_ai_roadmaps` table), interactively mark milestones as `NOT_STARTED`, `IN_PROGRESS`, or `COMPLETED`, and monitor real-time completion percentages.

---


## 3. Prerequisites

1. **Java Development Kit (JDK):** Version 17 or higher
2. **Apache Maven:** Version 3.8+ (or included `mvnw.cmd` wrapper)
3. **Node.js & npm:** Node 18+ and npm 9+ (for running the React frontend)
4. **MySQL Server:** Version 8.0+ running on port `3306`

---

## 4. Database Setup

SkillForge uses MySQL 8 with the database name `skillforge2_db`.

### Option A: Automatic Creation (Default)
The Spring Boot configuration includes `createDatabaseIfNotExist=true` and Hibernate DDL `update`, so the schema and tables are created automatically upon application launch.

### Option B: Manual Creation
Execute in MySQL Workbench or CLI:
```sql
CREATE DATABASE IF NOT EXISTS `skillforge2_db` 
  DEFAULT CHARACTER SET utf8mb4 
  COLLATE utf8mb4_unicode_ci;
```

### Credentials & Token Configuration (`src/main/resources/application.properties`)
```properties
spring.application.name=SkillForge
server.port=8080

# MySQL Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/skillforge2_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=sai052006
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA / Hibernate Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
spring.jpa.open-in-view=true

# Security & JWT Token Rotation
jwt.secret=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
jwt.expiration=900000            # 15 minutes access token lifetime
jwt.refresh-expiration=604800000 # 7 days refresh token lifetime
jwt.cookie-name=SF_AUTH_TOKEN
jwt.refresh-cookie-name=SF_REFRESH_TOKEN

# File Upload Settings for Resumes (PDF / DOCX)
spring.servlet.multipart.max-file-size=15MB
spring.servlet.multipart.max-request-size=15MB

app.college.allowed-domain=*
```

---

## 5. Running the Application

### 1. Start the Spring Boot Backend
Open a terminal in `skillforge(2)`:
```bash
mvn spring-boot:run
```
The backend server will start at **`http://localhost:8080`**.
On first startup, `DataSeeder` automatically populates the database with:
- 1 Admin account & 1 Sample Student account
- 4 Career Roles (`Java Backend Developer`, `Full Stack Developer`, `Data Analyst`, `QA Automation Engineer`)
- 40+ Skills cataloged across Languages, Frameworks, Databases, and Tools
- Weighted requirements per role (CORE = 10 pts, GOOD_TO_HAVE = 4 pts)
- Sequenced gap roadmap milestones per role

### 2. Start the React Frontend
Open a second terminal in `skillforge(2)/frontend`:
```bash
npm install
npm run dev
```
The React development server will start at **`http://localhost:5173`** (preconfigured with Vite proxy to `http://localhost:8080/api`).

---

## 6. Seeded Logins & Testing Accounts

| Role | Email Address | Password | Permissions |
|---|---|---|---|
| **Student** | `student@college.edu` | `Student@123` | Dashboard, Profile Builder, Resume Parser, Readiness Gap Report, Roadmap Tracker |
| **Administrator** | `admin@college.edu` | `Admin@123` | Placement Analytics & Heatmap, Candidate Roster CSV Export, Role & Skill Controls |

---

## 7. Main URLs & Navigation

### React Single-Page Application (`http://localhost:5173`)
- **Landing Page:** `http://localhost:5173`
- **Student Dashboard:** Real-time score meter, missing competency split, active milestones
- **Profile Builder:** Education, benchmark track, project links, and **AI Resume Parser dropzone** with confirmation preview
- **Readiness Report:** Two-tier breakdown (Core vs. Good-to-Have) with proficiency multipliers
- **Roadmap Tracker:** Personalized gap-driven milestones with segmented status toggles (`Not started`, `In progress`, `Completed`)
- **Placement Analytics & Heatmap (Admin):** Executive KPI cards, Department vs. Role readiness heatmap, gap category bars, and filterable candidate roster with one-click CSV export
- **Role & Skill Controls (Admin):** CRUD career tracks, weighted requirements, sequenced milestones, and master catalog

### Thymeleaf Pages (`http://localhost:8080`)
- **Public Directory:** `http://localhost:8080/`
- **Sign In:** `http://localhost:8080/login`
- **Register Candidate:** `http://localhost:8080/register`
- **Student Dashboard:** `http://localhost:8080/dashboard`
- **Profile Builder:** `http://localhost:8080/profile`
- **Readiness Gap Report:** `http://localhost:8080/readiness`
- **Roadmap Tracker:** `http://localhost:8080/roadmap`
- **Admin Roles Console:** `http://localhost:8080/admin/roles`

---

## 8. REST API Reference (`/api/**`)

### Authentication & Token Rotation
- `POST /api/auth/register` — Register a student account
- `POST /api/auth/login` — Authenticate and receive `token` + `refreshToken`
- `POST /api/auth/refresh` — Rotate refresh token and obtain new access token
- `GET  /api/auth/me` — Get current user details and role
- `POST /api/auth/logout` — Revoke active refresh token and wipe session cookies

### Candidate Profile & AI Resume Parser
- `GET    /api/profile` — Fetch candidate profile, acquired skills, and projects
- `POST   /api/profile/upload-resume` — Multipart upload (`.pdf`/`.docx`) parsed via Apache Tika
- `POST   /api/profile/apply-resume-skills` — Persist confirmed resume competencies & recalculate readiness
- `POST   /api/profile/target-role?roleId={id}` — Update target career role benchmark
- `POST   /api/profile/skills` — Add individual catalog skill with proficiency
- `DELETE /api/profile/skills/{id}` — Remove skill from profile
- `POST   /api/profile/projects` & `DELETE /api/profile/projects/{id}` — Manage practical projects

### Readiness & Roadmap
- `GET  /api/readiness` — Computed readiness percentage, total weights, and missing competency gap arrays
- `GET  /api/roadmap` — Sequenced learning milestones generated strictly from unacquired gaps
- `POST /api/roadmap/update?roadmapId={id}&status={status}` — Toggle milestone status (`NOT_STARTED`, `IN_PROGRESS`, `COMPLETED`); completing auto-awards the competency and refreshes score

### Placement Admin Analytics & Heatmap (Admin Only)
- `GET /api/admin/analytics/batch-summary` — Cohort metrics, job-ready percentage, critical core gaps, category deficits
- `GET /api/admin/analytics/heatmap` — Department $\times$ Role matrix with candidate density and average scores
- `GET /api/admin/analytics/candidates?minScore={score}&roleId={id}` — Filterable recruiter roster
- `GET /api/admin/export/candidates?minScore={score}&roleId={id}` — Download formatted RFC 4180 CSV file

---

## 9. Business Logic & Scoring Rules

1. **Readiness Score Formula:**
   $$\text{Score} = \left(\frac{\sum \text{Earned Weights of Acquired Skills}}{\sum \text{Total Weights of Role Requirements}}\right) \times 100$$
2. **Proficiency Multipliers:**
   - `BEGINNER`: $0.5\times$
   - `INTERMEDIATE`: $0.8\times$
   - `ADVANCED`: $1.0\times$
3. **Weight Calibration:**
   - `CORE` requirement: $10\text{ points}$
   - `GOOD_TO_HAVE` requirement: $4\text{ points}$
4. **Real-Time Recalculation Triggers:**
   - Adding, deleting, or adjusting a skill's proficiency in the Profile Builder.
   - Parsing and applying competencies from a resume.
   - Changing the benchmark career role track.
   - Marking a roadmap milestone `COMPLETED` (auto-awards the skill as `BEGINNER` and increments score immediately).

---

## 10. Automated Test Suite Execution

You can run the end-to-end PowerShell automated test script to verify all modules:
```powershell
powershell -ExecutionPolicy Bypass -File "scratch/verify_enterprise_features.ps1"
```
This tests:
1. Student login and JWT refresh token rotation via `POST /api/auth/refresh`.
2. Token reuse / replay attack rejection with HTTP `403 Forbidden`.
3. Apache Tika resume parsing with proficiency extraction and dynamic score recalculation.
4. Admin authentication, batch summary metrics, department heatmap cells, and CSV candidate roster generation.
