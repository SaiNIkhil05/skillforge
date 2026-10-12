package com.skillforge.config;

import com.skillforge.model.*;
import com.skillforge.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final CareerRoleRepository roleRepository;
    private final SkillRepository skillRepository;
    private final RoleRequiredSkillRepository requiredSkillRepository;
    private final RoadmapItemRepository roadmapItemRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(CareerRoleRepository roleRepository,
                      SkillRepository skillRepository,
                      RoleRequiredSkillRepository requiredSkillRepository,
                      RoadmapItemRepository roadmapItemRepository,
                      UserRepository userRepository,
                      PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.skillRepository = skillRepository;
        this.requiredSkillRepository = requiredSkillRepository;
        this.roadmapItemRepository = roadmapItemRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (roleRepository.count() > 0) {
            log.info("Database already seeded with career roles. Skipping seeder.");
            return;
        }

        log.info("Starting SkillForge database seeding...");

        // 1. Seed Skills Master Catalog (40+ skills across 4 categories)
        Map<String, Skill> s = new HashMap<>();

        // LANGUAGES
        s.put("Java", skillRepository.save(new Skill(null, "Java", SkillCategory.LANGUAGE)));
        s.put("SQL", skillRepository.save(new Skill(null, "SQL", SkillCategory.LANGUAGE)));
        s.put("Python", skillRepository.save(new Skill(null, "Python", SkillCategory.LANGUAGE)));
        s.put("JavaScript", skillRepository.save(new Skill(null, "JavaScript", SkillCategory.LANGUAGE)));
        s.put("TypeScript", skillRepository.save(new Skill(null, "TypeScript", SkillCategory.LANGUAGE)));
        s.put("HTML5", skillRepository.save(new Skill(null, "HTML5", SkillCategory.LANGUAGE)));
        s.put("CSS3", skillRepository.save(new Skill(null, "CSS3", SkillCategory.LANGUAGE)));
        s.put("Bash/Shell", skillRepository.save(new Skill(null, "Bash/Shell", SkillCategory.LANGUAGE)));

        // FRAMEWORKS
        s.put("Spring Boot", skillRepository.save(new Skill(null, "Spring Boot", SkillCategory.FRAMEWORK)));
        s.put("Spring Security", skillRepository.save(new Skill(null, "Spring Security", SkillCategory.FRAMEWORK)));
        s.put("Spring Data JPA", skillRepository.save(new Skill(null, "Spring Data JPA", SkillCategory.FRAMEWORK)));
        s.put("Hibernate", skillRepository.save(new Skill(null, "Hibernate", SkillCategory.FRAMEWORK)));
        s.put("React", skillRepository.save(new Skill(null, "React", SkillCategory.FRAMEWORK)));
        s.put("JUnit 5", skillRepository.save(new Skill(null, "JUnit 5", SkillCategory.FRAMEWORK)));
        s.put("Mockito", skillRepository.save(new Skill(null, "Mockito", SkillCategory.FRAMEWORK)));
        s.put("Pandas", skillRepository.save(new Skill(null, "Pandas", SkillCategory.FRAMEWORK)));
        s.put("NumPy", skillRepository.save(new Skill(null, "NumPy", SkillCategory.FRAMEWORK)));
        s.put("Selenium", skillRepository.save(new Skill(null, "Selenium", SkillCategory.FRAMEWORK)));
        s.put("TestNG", skillRepository.save(new Skill(null, "TestNG", SkillCategory.FRAMEWORK)));
        s.put("Playwright", skillRepository.save(new Skill(null, "Playwright", SkillCategory.FRAMEWORK)));
        s.put("Express.js", skillRepository.save(new Skill(null, "Express.js", SkillCategory.FRAMEWORK)));

        // DATABASES
        s.put("MySQL", skillRepository.save(new Skill(null, "MySQL", SkillCategory.DATABASE)));
        s.put("PostgreSQL", skillRepository.save(new Skill(null, "PostgreSQL", SkillCategory.DATABASE)));
        s.put("MongoDB", skillRepository.save(new Skill(null, "MongoDB", SkillCategory.DATABASE)));
        s.put("Redis", skillRepository.save(new Skill(null, "Redis", SkillCategory.DATABASE)));
        s.put("Database Indexing & Query Tuning", skillRepository.save(new Skill(null, "Database Indexing & Query Tuning", SkillCategory.DATABASE)));

        // TOOLS & PRACTICES
        s.put("Git", skillRepository.save(new Skill(null, "Git", SkillCategory.TOOLS_AND_PRACTICES)));
        s.put("GitHub", skillRepository.save(new Skill(null, "GitHub", SkillCategory.TOOLS_AND_PRACTICES)));
        s.put("Maven", skillRepository.save(new Skill(null, "Maven", SkillCategory.TOOLS_AND_PRACTICES)));
        s.put("Docker", skillRepository.save(new Skill(null, "Docker", SkillCategory.TOOLS_AND_PRACTICES)));
        s.put("REST APIs", skillRepository.save(new Skill(null, "REST APIs", SkillCategory.TOOLS_AND_PRACTICES)));
        s.put("Microservices", skillRepository.save(new Skill(null, "Microservices", SkillCategory.TOOLS_AND_PRACTICES)));
        s.put("Postman", skillRepository.save(new Skill(null, "Postman", SkillCategory.TOOLS_AND_PRACTICES)));
        s.put("CI/CD Pipelines", skillRepository.save(new Skill(null, "CI/CD Pipelines", SkillCategory.TOOLS_AND_PRACTICES)));
        s.put("Linux Fundamentals", skillRepository.save(new Skill(null, "Linux Fundamentals", SkillCategory.TOOLS_AND_PRACTICES)));
        s.put("Agile / Scrum", skillRepository.save(new Skill(null, "Agile / Scrum", SkillCategory.TOOLS_AND_PRACTICES)));
        s.put("API Documentation (Swagger/OpenAPI)", skillRepository.save(new Skill(null, "API Documentation (Swagger/OpenAPI)", SkillCategory.TOOLS_AND_PRACTICES)));
        s.put("Data Visualization (Tableau/PowerBI)", skillRepository.save(new Skill(null, "Data Visualization (Tableau/PowerBI)", SkillCategory.TOOLS_AND_PRACTICES)));
        s.put("Statistical Analysis", skillRepository.save(new Skill(null, "Statistical Analysis", SkillCategory.TOOLS_AND_PRACTICES)));
        s.put("Automated Testing Frameworks", skillRepository.save(new Skill(null, "Automated Testing Frameworks", SkillCategory.TOOLS_AND_PRACTICES)));
        s.put("Kubernetes Basics", skillRepository.save(new Skill(null, "Kubernetes Basics", SkillCategory.TOOLS_AND_PRACTICES)));

        // 2. Seed Career Roles (4 Roles)
        CareerRole javaRole = roleRepository.save(new CareerRole(null, "Java Backend Developer",
                "Designs, constructs, and deploys high-throughput server-side applications, secure REST APIs, and microservices leveraging Java, Spring Boot, and enterprise SQL databases."));

        CareerRole fullStackRole = roleRepository.save(new CareerRole(null, "Full Stack Developer",
                "Delivers end-to-end web applications bridging modern responsive client interfaces with secure, scalable backend web services and relational database persistence."));

        CareerRole dataAnalystRole = roleRepository.save(new CareerRole(null, "Data Analyst",
                "Transforms raw transactional and business data into high-value decision models, dashboards, and automated metric pipelines using SQL, Python, and statistical toolkits."));

        CareerRole qaRole = roleRepository.save(new CareerRole(null, "QA Automation Engineer",
                "Architects and maintains automated regression suites, API test benches, and continuous quality gates in modern delivery pipelines to eliminate production defects."));

        // 3. Required Skills per Role (10-15 per role, CORE=10, GOOD_TO_HAVE=4)
        // Java Backend Developer
        addRequired(javaRole, s.get("Java"), SkillPriority.CORE, 10);
        addRequired(javaRole, s.get("Spring Boot"), SkillPriority.CORE, 10);
        addRequired(javaRole, s.get("Spring Data JPA"), SkillPriority.CORE, 10);
        addRequired(javaRole, s.get("SQL"), SkillPriority.CORE, 10);
        addRequired(javaRole, s.get("REST APIs"), SkillPriority.CORE, 10);
        addRequired(javaRole, s.get("Git"), SkillPriority.CORE, 10);
        addRequired(javaRole, s.get("Spring Security"), SkillPriority.GOOD_TO_HAVE, 4);
        addRequired(javaRole, s.get("MySQL"), SkillPriority.GOOD_TO_HAVE, 4);
        addRequired(javaRole, s.get("Maven"), SkillPriority.GOOD_TO_HAVE, 4);
        addRequired(javaRole, s.get("JUnit 5"), SkillPriority.GOOD_TO_HAVE, 4);
        addRequired(javaRole, s.get("Mockito"), SkillPriority.GOOD_TO_HAVE, 4);
        addRequired(javaRole, s.get("Docker"), SkillPriority.GOOD_TO_HAVE, 4);
        addRequired(javaRole, s.get("Microservices"), SkillPriority.GOOD_TO_HAVE, 4);
        addRequired(javaRole, s.get("Postman"), SkillPriority.GOOD_TO_HAVE, 4);

        // Full Stack Developer
        addRequired(fullStackRole, s.get("JavaScript"), SkillPriority.CORE, 10);
        addRequired(fullStackRole, s.get("React"), SkillPriority.CORE, 10);
        addRequired(fullStackRole, s.get("Java"), SkillPriority.CORE, 10);
        addRequired(fullStackRole, s.get("Spring Boot"), SkillPriority.CORE, 10);
        addRequired(fullStackRole, s.get("HTML5"), SkillPriority.CORE, 10);
        addRequired(fullStackRole, s.get("CSS3"), SkillPriority.CORE, 10);
        addRequired(fullStackRole, s.get("REST APIs"), SkillPriority.CORE, 10);
        addRequired(fullStackRole, s.get("SQL"), SkillPriority.GOOD_TO_HAVE, 4);
        addRequired(fullStackRole, s.get("TypeScript"), SkillPriority.GOOD_TO_HAVE, 4);
        addRequired(fullStackRole, s.get("Git"), SkillPriority.GOOD_TO_HAVE, 4);
        addRequired(fullStackRole, s.get("PostgreSQL"), SkillPriority.GOOD_TO_HAVE, 4);
        addRequired(fullStackRole, s.get("Docker"), SkillPriority.GOOD_TO_HAVE, 4);

        // Data Analyst
        addRequired(dataAnalystRole, s.get("SQL"), SkillPriority.CORE, 10);
        addRequired(dataAnalystRole, s.get("Python"), SkillPriority.CORE, 10);
        addRequired(dataAnalystRole, s.get("Pandas"), SkillPriority.CORE, 10);
        addRequired(dataAnalystRole, s.get("Statistical Analysis"), SkillPriority.CORE, 10);
        addRequired(dataAnalystRole, s.get("Data Visualization (Tableau/PowerBI)"), SkillPriority.CORE, 10);
        addRequired(dataAnalystRole, s.get("NumPy"), SkillPriority.GOOD_TO_HAVE, 4);
        addRequired(dataAnalystRole, s.get("MySQL"), SkillPriority.GOOD_TO_HAVE, 4);
        addRequired(dataAnalystRole, s.get("Git"), SkillPriority.GOOD_TO_HAVE, 4);
        addRequired(dataAnalystRole, s.get("Database Indexing & Query Tuning"), SkillPriority.GOOD_TO_HAVE, 4);

        // QA Automation Engineer
        addRequired(qaRole, s.get("Java"), SkillPriority.CORE, 10);
        addRequired(qaRole, s.get("Selenium"), SkillPriority.CORE, 10);
        addRequired(qaRole, s.get("JUnit 5"), SkillPriority.CORE, 10);
        addRequired(qaRole, s.get("Automated Testing Frameworks"), SkillPriority.CORE, 10);
        addRequired(qaRole, s.get("Git"), SkillPriority.CORE, 10);
        addRequired(qaRole, s.get("Postman"), SkillPriority.GOOD_TO_HAVE, 4);
        addRequired(qaRole, s.get("REST APIs"), SkillPriority.GOOD_TO_HAVE, 4);
        addRequired(qaRole, s.get("CI/CD Pipelines"), SkillPriority.GOOD_TO_HAVE, 4);
        addRequired(qaRole, s.get("Playwright"), SkillPriority.GOOD_TO_HAVE, 4);
        addRequired(qaRole, s.get("TestNG"), SkillPriority.GOOD_TO_HAVE, 4);

        // 4. Roadmap Items per Role (8-12 items each, sequenced with orderIndex)
        // Java Backend Developer Roadmap
        addRoadmap(javaRole, s.get("Java"), "Master Java OOP, Collections Framework, Generics, and Streams API", 1);
        addRoadmap(javaRole, s.get("SQL"), "Write complex SQL joins, aggregations, window functions, and indexing", 2);
        addRoadmap(javaRole, s.get("Maven"), "Manage dependency trees, build profiles, and plugins with Apache Maven", 3);
        addRoadmap(javaRole, s.get("Spring Boot"), "Build RESTful Web Services with Spring Boot 3 controllers and error handlers", 4);
        addRoadmap(javaRole, s.get("Spring Data JPA"), "Implement database repositories and entity relations with Hibernate & Spring Data JPA", 5);
        addRoadmap(javaRole, s.get("Spring Security"), "Secure APIs using Spring Security 6 stateless JWT authentication and role guards", 6);
        addRoadmap(javaRole, s.get("JUnit 5"), "Write unit and integration tests using JUnit 5 assertions and test lifecycle", 7);
        addRoadmap(javaRole, s.get("Mockito"), "Mock service dependencies and verify repository interactions in tests", 8);
        addRoadmap(javaRole, s.get("Docker"), "Containerize Spring Boot apps with multi-stage Dockerfiles and compose setups", 9);
        addRoadmap(javaRole, s.get("Microservices"), "Decompose monolithic services into decoupled microservices with circuit breakers", 10);

        // Full Stack Developer Roadmap
        addRoadmap(fullStackRole, s.get("HTML5"), "Structure accessible, semantic web documents adhering to modern standards", 1);
        addRoadmap(fullStackRole, s.get("CSS3"), "Master responsive CSS layouts, Flexbox, Grid, and design tokens", 2);
        addRoadmap(fullStackRole, s.get("JavaScript"), "Deep-dive ES6+ JavaScript: asynchronous promises, async/await, closures", 3);
        addRoadmap(fullStackRole, s.get("React"), "Build component hierarchies, hooks state management, and client routing in React", 4);
        addRoadmap(fullStackRole, s.get("Java"), "Configure Java 17 enterprise fundamentals and data structures", 5);
        addRoadmap(fullStackRole, s.get("Spring Boot"), "Construct REST APIs serving JSON payloads with Spring Boot", 6);
        addRoadmap(fullStackRole, s.get("TypeScript"), "Add static type safety to React components, props, and API interfaces", 7);
        addRoadmap(fullStackRole, s.get("PostgreSQL"), "Design normalized relational schemas and write transactional queries", 8);
        addRoadmap(fullStackRole, s.get("Docker"), "Package frontend bundle and backend JAR into containerized environments", 9);

        // Data Analyst Roadmap
        addRoadmap(dataAnalystRole, s.get("SQL"), "Execute multi-table joins, subqueries, and aggregation group-by analyses", 1);
        addRoadmap(dataAnalystRole, s.get("Python"), "Script automated data extraction, file I/O, and data cleansing routines", 2);
        addRoadmap(dataAnalystRole, s.get("Pandas"), "Perform dataframe transformations, missing value handling, and grouping in Pandas", 3);
        addRoadmap(dataAnalystRole, s.get("NumPy"), "Compute vector mathematical operations and matrix manipulations in NumPy", 4);
        addRoadmap(dataAnalystRole, s.get("Statistical Analysis"), "Apply hypothesis testing, correlation analysis, and descriptive distributions", 5);
        addRoadmap(dataAnalystRole, s.get("Data Visualization (Tableau/PowerBI)"), "Build interactive executive dashboards and storytelling charts", 6);
        addRoadmap(dataAnalystRole, s.get("Database Indexing & Query Tuning"), "Analyze query execution plans and optimize slow SQL reports", 7);

        // QA Automation Engineer Roadmap
        addRoadmap(qaRole, s.get("Java"), "Review Java core programming, object design, and exception handling", 1);
        addRoadmap(qaRole, s.get("JUnit 5"), "Structure test fixtures, assertions, and parameterized test executions", 2);
        addRoadmap(qaRole, s.get("Selenium"), "Automate browser interactions using Selenium WebDriver and Page Object Model", 3);
        addRoadmap(qaRole, s.get("Automated Testing Frameworks"), "Architect reusable hybrid test automation frameworks with reporting", 4);
        addRoadmap(qaRole, s.get("Postman"), "Author API test collections with pre-request scripts and response assertions", 5);
        addRoadmap(qaRole, s.get("Playwright"), "Execute fast, resilient end-to-end web tests using modern Playwright APIs", 6);
        addRoadmap(qaRole, s.get("CI/CD Pipelines"), "Integrate automated smoke and regression test triggers into CI/CD pipelines", 7);

        // 5. Seed Users (1 Admin + 1 Student)
        User admin = new User();
        admin.setFullName("Administrator");
        admin.setEmail("admin@college.edu");
        admin.setPasswordHash(passwordEncoder.encode("Admin@123"));
        admin.setRole(UserRole.ADMIN);
        admin.setEducation("College Placement & Career Center");
        admin.setEmailVerified(true);
        admin.setCreatedAt(LocalDateTime.now());
        userRepository.save(admin);

        User student = new User();
        student.setFullName("Sai Nikhil");
        student.setEmail("student@college.edu");
        student.setPasswordHash(passwordEncoder.encode("Student@123"));
        student.setRole(UserRole.STUDENT);
        student.setEducation("B.Tech Computer Science & Engineering, 2025");
        student.setTargetRole(javaRole);
        student.setEmailVerified(true);
        student.setCreatedAt(LocalDateTime.now());
        userRepository.save(student);

        log.info("SkillForge database seeded successfully!");
        log.info("Admin account: admin@college.edu / Admin@123");
        log.info("Student account: student@college.edu / Student@123");
    }

    private void addRequired(CareerRole role, Skill skill, SkillPriority priority, Integer weight) {
        RoleRequiredSkill rrs = new RoleRequiredSkill(null, role, skill, priority, weight);
        requiredSkillRepository.save(rrs);
    }

    private void addRoadmap(CareerRole role, Skill skill, String title, Integer orderIndex) {
        RoadmapItem item = new RoadmapItem(null, role, skill, title, orderIndex);
        roadmapItemRepository.save(item);
    }
}
