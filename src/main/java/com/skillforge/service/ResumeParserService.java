package com.skillforge.service;

import com.skillforge.dto.*;
import com.skillforge.exception.ResourceNotFoundException;
import com.skillforge.model.*;
import com.skillforge.repository.RoleRequiredSkillRepository;
import com.skillforge.repository.SkillRepository;
import com.skillforge.repository.UserSkillRepository;
import org.apache.tika.Tika;
import org.apache.tika.exception.EncryptedDocumentException;
import org.apache.tika.exception.TikaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class ResumeParserService {

    private static final Logger log = LoggerFactory.getLogger(ResumeParserService.class);
    private static final long MAX_FILE_SIZE_BYTES = 15 * 1024 * 1024; // 15 MB

    private final SkillRepository skillRepository;
    private final UserSkillRepository userSkillRepository;
    private final RoleRequiredSkillRepository roleRequiredSkillRepository;
    private final ReadinessScoreService readinessScoreService;
    private final Tika tika;

    // Advanced proficiency & impact indicators
    private static final Set<String> ADVANCED_SIGNALS = Set.of(
            "architected", "lead", "senior", "production", "scalable", "optimization", "optimized",
            "refactored", "high-throughput", "concurrency", "distributed", "5+ years", "4+ years",
            "mastery", "zero-downtime", "migrated", "performance tuning", "low-latency", "microservices"
    );

    // Intermediate hands-on indicators
    private static final Set<String> INTERMEDIATE_SIGNALS = Set.of(
            "built", "developed", "implemented", "designed", "integrated", "created", "maintained",
            "hands-on", "proficient", "automated", "tested", "configured", "deployed", "2+ years", "3+ years",
            "full-stack", "backend", "frontend", "engineered"
    );

    // Beginner / exposure dampeners
    private static final Set<String> BEGINNER_SIGNALS = Set.of(
            "beginner", "basics", "introductory", "coursework", "familiar with", "exposure to",
            "learning", "academic", "novice", "fundamentals"
    );

    // Common certification patterns
    private static final List<Pattern> CERTIFICATION_PATTERNS = List.of(
            Pattern.compile("(?i)AWS Certified [A-Za-z\\s]+"),
            Pattern.compile("(?i)Microsoft Certified:?\\s*[A-Za-z\\s]+"),
            Pattern.compile("(?i)Oracle Certified\\s*[A-Za-z\\s]+"),
            Pattern.compile("(?i)Certified Kubernetes (Administrator|Application Developer|CKA|CKAD)"),
            Pattern.compile("(?i)Certified Scrum Master|CSM"),
            Pattern.compile("(?i)Google Cloud (Associate|Professional)\\s*[A-Za-z\\s]+"),
            Pattern.compile("(?i)CompTIA (Security\\+|Network\\+|A\\+)")
    );

    public ResumeParserService(
            SkillRepository skillRepository,
            UserSkillRepository userSkillRepository,
            RoleRequiredSkillRepository roleRequiredSkillRepository,
            ReadinessScoreService readinessScoreService) {
        this.skillRepository = skillRepository;
        this.userSkillRepository = userSkillRepository;
        this.roleRequiredSkillRepository = roleRequiredSkillRepository;
        this.readinessScoreService = readinessScoreService;
        this.tika = new Tika();
    }

    public ResumeParseResponse parseResume(MultipartFile file, User user) {
        // 1. Rigorous File Validation
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Resume document must not be empty.");
        }

        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new IllegalArgumentException(String.format("File size (%d MB) exceeds the maximum allowed limit of 15 MB.",
                    file.getSize() / (1024 * 1024)));
        }

        String originalFilename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "resume.pdf";
        String lowerFilename = originalFilename.toLowerCase();
        if (!lowerFilename.endsWith(".pdf") && !lowerFilename.endsWith(".docx") &&
            !lowerFilename.endsWith(".doc") && !lowerFilename.endsWith(".txt")) {
            throw new IllegalArgumentException("Unsupported file format. Please upload a PDF (.pdf), Microsoft Word (.docx, .doc), or Text (.txt) file.");
        }

        // 2. Apache Tika Text Extraction with Graceful Error Handling
        String extractedText;
        try (InputStream is = file.getInputStream()) {
            extractedText = tika.parseToString(is);
        } catch (EncryptedDocumentException ex) {
            log.warn("Password protected document detected: {}", originalFilename);
            throw new IllegalArgumentException("The uploaded document is password-protected. Please remove password encryption and re-upload.");
        } catch (TikaException ex) {
            log.error("Tika extraction failure: {}", ex.getMessage(), ex);
            throw new IllegalArgumentException("Could not read text from this document. The file may be corrupted or unreadable.");
        } catch (Exception ex) {
            log.error("Unexpected error during resume extraction: {}", ex.getMessage(), ex);
            throw new RuntimeException("Document processing error: " + ex.getMessage(), ex);
        }

        if (extractedText == null || extractedText.trim().length() < 30) {
            throw new IllegalArgumentException("No readable text could be extracted. The document may be a scanned image without selectable text. Please upload a searchable text-based document.");
        }

        // 3. Section Segmentation (Projects, Experience, Skills, Education, Certifications)
        Map<String, String> sections = extractResumeSections(extractedText);
        List<String> detectedSectionNames = new ArrayList<>(sections.keySet());

        // 4. Certification Extraction
        List<String> detectedCertifications = extractCertifications(extractedText);

        // Clean text for whole-document searching
        String cleanedText = extractedText.replaceAll("\\r?\\n", " ").replaceAll("\\s+", " ");
        String lowerCleanedText = cleanedText.toLowerCase();

        // 5. Load Catalog Skills & User's Existing Profile
        List<Skill> catalogSkills = skillRepository.findAll();
        Map<Integer, UserSkill> existingUserSkills = userSkillRepository.findByUser(user).stream()
                .collect(Collectors.toMap(us -> us.getSkill().getSkillId(), us -> us, (a, b) -> a));

        // 6. Target Role Benchmark Requirements (Core vs Good-to-Have)
        CareerRole targetRole = user.getTargetRole();
        Map<Integer, RoleRequiredSkill> targetRoleRequirements = new HashMap<>();
        List<String> allTargetRoleCoreSkillNames = new ArrayList<>();

        if (targetRole != null) {
            List<RoleRequiredSkill> required = roleRequiredSkillRepository.findByCareerRole(targetRole);
            for (RoleRequiredSkill rrs : required) {
                targetRoleRequirements.put(rrs.getSkill().getSkillId(), rrs);
                if (rrs.getPriority() == SkillPriority.CORE) {
                    allTargetRoleCoreSkillNames.add(rrs.getSkill().getSkillName());
                }
            }
        }

        List<ParsedSkillDTO> detectedSkills = new ArrayList<>();
        Set<String> matchedSkillNames = new HashSet<>();

        // 7. Context-Aware Competency Detection
        for (Skill skill : catalogSkills) {
            String skillName = skill.getSkillName().trim();
            SkillExtractionResult result = evaluateSkillAcrossDocument(skillName, skill.getCategory(), cleanedText, lowerCleanedText, sections);

            if (result.matched) {
                matchedSkillNames.add(skillName);
                UserSkill existing = existingUserSkills.get(skill.getSkillId());
                boolean alreadyInProfile = (existing != null);

                ParsedSkillDTO dto = new ParsedSkillDTO(
                        skill.getSkillId(),
                        skill.getSkillName(),
                        skill.getCategory(),
                        result.estimatedProficiency,
                        result.confidence,
                        result.evidenceSnippet,
                        alreadyInProfile
                );

                dto.setEvidenceSnippet(result.evidenceSnippet);
                dto.setSectionName(result.sectionName);
                dto.setEvidenceType(result.evidenceType);
                dto.setReasoning(result.reasoning);
                dto.setNeedsReview(result.needsReview);

                // Profile Comparison
                if (existing != null) {
                    dto.setCurrentProficiency(existing.getProficiency());
                    if (result.estimatedProficiency.ordinal() > existing.getProficiency().ordinal()) {
                        dto.setProfileStatus("UPGRADE_SUGGESTED");
                        dto.setSelected(true); // Suggest upgrading
                    } else if (result.estimatedProficiency.ordinal() == existing.getProficiency().ordinal()) {
                        dto.setProfileStatus("EXISTING_CONFIRMED");
                        dto.setSelected(false); // Already confirmed at this level
                    } else {
                        dto.setProfileStatus("DISCREPANCY_FLAGGED");
                        dto.setNeedsReview(true);
                        dto.setSelected(false); // Do not downgrade automatically
                    }
                } else {
                    dto.setProfileStatus("NEW_COMPETENCY");
                    dto.setSelected(true);
                }

                // Target Role Relevance
                RoleRequiredSkill req = targetRoleRequirements.get(skill.getSkillId());
                if (req != null) {
                    dto.setTargetRoleRequired(true);
                    dto.setTargetRolePriority(req.getPriority());
                    dto.setTargetRoleWeight(req.getWeight());
                } else {
                    dto.setTargetRoleRequired(false);
                }

                detectedSkills.add(dto);
            }
        }

        // 8. Calculate Target Role Core Skills Covered vs Missing
        List<String> missingCoreSkills = new ArrayList<>();
        int targetRoleCoreCoveredCount = 0;
        for (String coreName : allTargetRoleCoreSkillNames) {
            if (matchedSkillNames.contains(coreName)) {
                targetRoleCoreCoveredCount++;
            } else {
                missingCoreSkills.add(coreName);
            }
        }

        // 9. Sort Detected Skills Intelligently:
        // Priority: Target Role Core first, then New Competencies, then Upgrades, then higher confidence
        detectedSkills.sort((a, b) -> {
            boolean aCore = a.isTargetRoleRequired() && a.getTargetRolePriority() == SkillPriority.CORE;
            boolean bCore = b.isTargetRoleRequired() && b.getTargetRolePriority() == SkillPriority.CORE;
            if (aCore != bCore) return aCore ? -1 : 1;

            if (a.isAlreadyInProfile() != b.isAlreadyInProfile()) {
                return a.isAlreadyInProfile() ? 1 : -1;
            }
            int conf = Double.compare(b.getConfidence(), a.getConfidence());
            if (conf != 0) return conf;
            return a.getSkillName().compareToIgnoreCase(b.getSkillName());
        });

        // 10. Summary Snippet and Potential Readiness Boost Estimate
        String snippet = cleanedText.length() > 320 ? cleanedText.substring(0, 320) + "..." : cleanedText;

        ResumeParseResponse response = new ResumeParseResponse(
                originalFilename,
                file.getContentType(),
                extractedText.length(),
                snippet,
                detectedSkills
        );

        response.setDetectedSections(detectedSectionNames);
        response.setDetectedCertifications(detectedCertifications);
        if (targetRole != null) {
            response.setTargetRoleName(targetRole.getRoleName());
            response.setTargetRoleCoreTotalCount(allTargetRoleCoreSkillNames.size());
            response.setTargetRoleCoreCoveredCount(targetRoleCoreCoveredCount);
            response.setMissingCoreSkills(missingCoreSkills);
        }

        // Estimate potential readiness boost from unacquired core/secondary skills
        double potentialBoost = 0;
        for (ParsedSkillDTO d : detectedSkills) {
            if (!d.isAlreadyInProfile() && d.isTargetRoleRequired() && d.getTargetRoleWeight() != null) {
                potentialBoost += d.getTargetRoleWeight();
            }
        }
        response.setEstimatedReadinessBoost(potentialBoost);
        response.recalculateCounts();

        return response;
    }

    @Transactional
    public Map<String, Object> applyResumeSkills(User user, List<ApplyResumeSkillsRequest.SkillItem> skillItems) {
        if (skillItems == null || skillItems.isEmpty()) {
            throw new IllegalArgumentException("No competencies selected to apply to profile.");
        }

        ReadinessScoreDTO beforeReadiness = readinessScoreService.calculateReadiness(user);
        int oldScore = beforeReadiness.getScore();

        int addedCount = 0;
        int updatedCount = 0;
        List<String> addedSkillNames = new ArrayList<>();
        List<String> updatedSkillNames = new ArrayList<>();

        for (ApplyResumeSkillsRequest.SkillItem item : skillItems) {
            if (item.getSkillId() == null) continue;

            Skill skill = skillRepository.findById(item.getSkillId())
                    .orElseThrow(() -> new ResourceNotFoundException("Skill not found with ID: " + item.getSkillId()));

            SkillProficiency proficiency = item.getProficiency() != null ? item.getProficiency() : SkillProficiency.BEGINNER;

            Optional<UserSkill> existing = userSkillRepository.findByUserAndSkill(user, skill);
            if (existing.isPresent()) {
                UserSkill us = existing.get();
                if (us.getProficiency() != proficiency) {
                    us.setProficiency(proficiency);
                    userSkillRepository.save(us);
                    updatedCount++;
                    updatedSkillNames.add(skill.getSkillName() + " (" + proficiency + ")");
                }
            } else {
                UserSkill us = new UserSkill();
                us.setUser(user);
                us.setSkill(skill);
                us.setProficiency(proficiency);
                userSkillRepository.save(us);
                addedCount++;
                addedSkillNames.add(skill.getSkillName() + " (" + proficiency + ")");
            }
        }

        // Recalculate candidate readiness score
        ReadinessScoreDTO newReadiness = readinessScoreService.calculateReadiness(user);
        int scoreIncrease = Math.max(0, newReadiness.getScore() - oldScore);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("addedCount", addedCount);
        result.put("updatedCount", updatedCount);
        result.put("totalProcessed", addedCount + updatedCount);
        result.put("oldScore", oldScore);
        result.put("newScore", newReadiness.getScore());
        result.put("scoreIncrease", scoreIncrease);
        result.put("readiness", newReadiness);
        result.put("addedSkills", addedSkillNames);
        result.put("updatedSkills", updatedSkillNames);
        result.put("message", String.format("Applied %d competencies (%d added, %d updated). Readiness score increased from %d%% to %d%% (+%d%%)!",
                addedCount + updatedCount, addedCount, updatedCount, oldScore, newReadiness.getScore(), scoreIncrease));

        return result;
    }

    // =========================================================================
    // SECTION SEGMENTATION & CERTIFICATION RECOGNITION
    // =========================================================================

    private Map<String, String> extractResumeSections(String text) {
        Map<String, String> sections = new LinkedHashMap<>();
        String[] lines = text.split("\\r?\\n");

        String currentSection = "SUMMARY";
        StringBuilder currentContent = new StringBuilder();

        Pattern headerPattern = Pattern.compile(
                "(?i)^[\\s#*•-]*(WORK\\s+EXPERIENCE|PROFESSIONAL\\s+EXPERIENCE|EXPERIENCE|EMPLOYMENT|INTERNSHIPS|PROJECTS|ACADEMIC\\s+PROJECTS|KEY\\s+PROJECTS|PERSONAL\\s+PROJECTS|TECHNICAL\\s+SKILLS|SKILLS|TECHNOLOGIES|TECH\\s+STACK|CORE\\s+COMPETENCIES|CERTIFICATIONS|LICENSES|EDUCATION|ACADEMIC\\s+BACKGROUND)[\\s:]*$"
        );

        for (String line : lines) {
            String trimmed = line.trim();
            Matcher matcher = headerPattern.matcher(trimmed);
            if (matcher.find()) {
                if (currentContent.length() > 0) {
                    sections.put(currentSection, currentContent.toString().trim());
                }
                String matchedHeader = matcher.group(1).toUpperCase();
                if (matchedHeader.contains("EXPERIENCE") || matchedHeader.contains("EMPLOYMENT") || matchedHeader.contains("INTERNSHIP")) {
                    currentSection = "EXPERIENCE";
                } else if (matchedHeader.contains("PROJECT")) {
                    currentSection = "PROJECTS";
                } else if (matchedHeader.contains("SKILL") || matchedHeader.contains("TECH") || matchedHeader.contains("COMPETENC")) {
                    currentSection = "SKILLS";
                } else if (matchedHeader.contains("CERTIF") || matchedHeader.contains("LICENSE")) {
                    currentSection = "CERTIFICATIONS";
                } else if (matchedHeader.contains("EDUCATION") || matchedHeader.contains("ACADEMIC")) {
                    currentSection = "EDUCATION";
                } else {
                    currentSection = matchedHeader;
                }
                currentContent = new StringBuilder();
            } else {
                currentContent.append(line).append(" ");
            }
        }

        if (currentContent.length() > 0) {
            sections.put(currentSection, currentContent.toString().trim());
        }

        return sections;
    }

    private List<String> extractCertifications(String text) {
        List<String> certs = new ArrayList<>();
        for (Pattern p : CERTIFICATION_PATTERNS) {
            Matcher m = p.matcher(text);
            while (m.find()) {
                String match = m.group().replaceAll("\\s+", " ").trim();
                if (!certs.contains(match)) {
                    certs.add(match);
                }
            }
        }
        return certs;
    }

    // =========================================================================
    // ADVANCED CONTEXT-AWARE SKILL EVALUATION
    // =========================================================================

    private SkillExtractionResult evaluateSkillAcrossDocument(
            String skillName,
            SkillCategory category,
            String cleanedText,
            String lowerCleanedText,
            Map<String, String> sections) {

        String lowerSkill = skillName.toLowerCase();
        List<Pattern> patterns = buildPatternsForSkill(lowerSkill);

        // Track best occurrence across the entire document
        SkillExtractionResult bestResult = new SkillExtractionResult(false, null, 0.0, null, null, null, null, false);

        // Priority order of sections: PROJECTS > EXPERIENCE > CERTIFICATIONS > SKILLS > OTHER
        List<String> sectionOrder = List.of("PROJECTS", "EXPERIENCE", "CERTIFICATIONS", "SKILLS", "SUMMARY", "EDUCATION");

        for (String secName : sectionOrder) {
            String secContent = sections.get(secName);
            if (secContent == null || secContent.isBlank()) continue;

            SkillExtractionResult secResult = searchInSection(secName, secContent, patterns, skillName);
            if (secResult.matched && secResult.confidence > bestResult.confidence) {
                bestResult = secResult;
            }
        }

        // If not found in labeled sections, search entire document
        if (!bestResult.matched) {
            SkillExtractionResult docResult = searchInSection("GENERAL", cleanedText, patterns, skillName);
            if (docResult.matched) {
                bestResult = docResult;
            }
        }

        return bestResult;
    }

    private SkillExtractionResult searchInSection(String sectionName, String sectionText, List<Pattern> patterns, String skillName) {
        String lowerSection = sectionText.toLowerCase();

        for (Pattern pattern : patterns) {
            Matcher matcher = pattern.matcher(lowerSection);
            if (matcher.find()) {
                int start = matcher.start();
                int end = matcher.end();

                // Extract sentence or 90-character contextual snippet
                int snippetStart = Math.max(0, start - 80);
                int snippetEnd = Math.min(sectionText.length(), end + 80);
                String rawSnippet = sectionText.substring(snippetStart, snippetEnd).trim();
                String snippet = (snippetStart > 0 ? "..." : "") + rawSnippet + (snippetEnd < sectionText.length() ? "..." : "");

                String contextLower = snippet.toLowerCase();

                // Multi-factor proficiency score calculation
                int score = 0;
                boolean hasAdvanced = false;
                boolean hasIntermediate = false;
                boolean hasBeginner = false;

                // 1. Action Verbs & Architectural Proof
                for (String adv : ADVANCED_SIGNALS) {
                    if (contextLower.contains(adv)) {
                        score += 35;
                        hasAdvanced = true;
                        break;
                    }
                }

                // 2. Intermediate Implementation Verbs
                for (String inter : INTERMEDIATE_SIGNALS) {
                    if (contextLower.contains(inter)) {
                        score += 20;
                        hasIntermediate = true;
                        break;
                    }
                }

                // 3. Quantitative Metrics (e.g. 30%, 10M, 100ms)
                if (Pattern.compile("\\d+[%mk]|latency|throughput|optimized").matcher(contextLower).find()) {
                    score += 15;
                }

                // 4. Section Weight
                String evidenceType = "GENERAL";
                if ("PROJECTS".equals(sectionName)) {
                    score += 25;
                    evidenceType = "PRACTICAL_PROJECT";
                } else if ("EXPERIENCE".equals(sectionName)) {
                    score += 25;
                    evidenceType = "WORK_EXPERIENCE";
                } else if ("CERTIFICATIONS".equals(sectionName)) {
                    score += 30;
                    evidenceType = "CERTIFICATION";
                } else if ("SKILLS".equals(sectionName)) {
                    score += 5;
                    evidenceType = "SKILLS_LISTING";
                }

                // 5. Beginner Dampeners
                for (String beg : BEGINNER_SIGNALS) {
                    if (contextLower.contains(beg)) {
                        score -= 20;
                        hasBeginner = true;
                        break;
                    }
                }

                SkillProficiency proficiency;
                double confidence;
                String reasoning;

                if (score >= 50) {
                    proficiency = SkillProficiency.ADVANCED;
                    confidence = 0.95;
                    reasoning = String.format("Demonstrated high-scale or architectural implementation in %s.",
                            "PROJECTS".equals(sectionName) ? "practical project work" : "professional experience");
                } else if (score >= 20) {
                    proficiency = SkillProficiency.INTERMEDIATE;
                    confidence = 0.90;
                    reasoning = String.format("Practical hands-on implementation evidenced in %s.",
                            "PROJECTS".equals(sectionName) ? "projects" : "work experience");
                } else {
                    proficiency = SkillProficiency.BEGINNER;
                    confidence = ("SKILLS".equals(sectionName)) ? 0.82 : 0.72;
                    reasoning = hasBeginner
                            ? "Listed with introductory or academic exposure keywords."
                            : "Cataloged competency without extensive production implementation evidence.";
                }

                boolean needsReview = (confidence < 0.75) || hasBeginner;

                return new SkillExtractionResult(true, proficiency, confidence, snippet, sectionName, evidenceType, reasoning, needsReview);
            }
        }

        return new SkillExtractionResult(false, null, 0.0, null, null, null, null, false);
    }

    private List<Pattern> buildPatternsForSkill(String lowerSkill) {
        List<Pattern> patterns = new ArrayList<>();

        // 1. Accurate symbol-handling for tricky technologies
        switch (lowerSkill) {
            case "c++":
                patterns.add(Pattern.compile("(?i)(?<=^|[^a-zA-Z0-9])c\\+\\+(?=[^a-zA-Z0-9]|$)"));
                return patterns;
            case "c#":
                patterns.add(Pattern.compile("(?i)(?<=^|[^a-zA-Z0-9])c\\#(?=[^a-zA-Z0-9]|$)"));
                return patterns;
            case ".net":
                patterns.add(Pattern.compile("(?i)(?<=^|[\\s,;/(\\[])\\.net(?=[\\s,;/)\\]]|$)"));
                return patterns;
            case "node.js":
                patterns.add(Pattern.compile("(?i)\\b(node\\.js|nodejs|node\\s+js)\\b"));
                return patterns;
            case "react":
                patterns.add(Pattern.compile("(?i)\\b(react|react\\.js|reactjs)\\b"));
                return patterns;
            case "spring boot":
                patterns.add(Pattern.compile("(?i)\\b(spring\\s+boot|springboot|spring-boot)\\b"));
                return patterns;
            case "spring data jpa":
                patterns.add(Pattern.compile("(?i)\\b(spring\\s+data\\s+jpa|spring\\s+data|spring\\s+jpa)\\b"));
                return patterns;
            case "spring security":
                patterns.add(Pattern.compile("(?i)\\bspring\\s+security\\b"));
                return patterns;
            case "postgresql":
                patterns.add(Pattern.compile("(?i)\\b(postgresql|postgres|psql)\\b"));
                return patterns;
            case "mongodb":
                patterns.add(Pattern.compile("(?i)\\b(mongodb|mongo)\\b"));
                return patterns;
            case "docker":
                patterns.add(Pattern.compile("(?i)\\b(docker|dockerfile|docker-compose|containerization)\\b"));
                return patterns;
            case "kubernetes basics":
                patterns.add(Pattern.compile("(?i)\\b(kubernetes|k8s)\\b"));
                return patterns;
            case "ci/cd pipelines":
                patterns.add(Pattern.compile("(?i)\\b(ci/cd|cicd|continuous\\s+integration|github\\s+actions|jenkins|gitlab\\s+ci)\\b"));
                return patterns;
            case "rest apis":
                patterns.add(Pattern.compile("(?i)\\b(rest\\s+api|rest\\s+apis|restful\\s+apis|restful\\s+api|restful)\\b"));
                return patterns;
            case "api documentation (swagger/openapi)":
                patterns.add(Pattern.compile("(?i)\\b(swagger|openapi|api\\s+documentation)\\b"));
                return patterns;
            case "database indexing & query tuning":
                patterns.add(Pattern.compile("(?i)\\b(query\\s+tuning|query\\s+optimization|database\\s+indexing|explain\\s+plan)\\b"));
                return patterns;
            case "data visualization (tableau/powerbi)":
                patterns.add(Pattern.compile("(?i)\\b(tableau|power\\s*bi|powerbi|data\\s+visualization|matplotlib|seaborn)\\b"));
                return patterns;
            case "automated testing frameworks":
                patterns.add(Pattern.compile("(?i)\\b(automated\\s+testing|test\\s+automation|automation\\s+framework|hybrid\\s+framework)\\b"));
                return patterns;
            case "java":
                // Boundary check: Java without Script
                patterns.add(Pattern.compile("(?i)\\bjava(?!script)\\b"));
                return patterns;
            case "javascript":
                patterns.add(Pattern.compile("(?i)\\b(javascript|ecmascript)\\b"));
                return patterns;
            case "typescript":
                patterns.add(Pattern.compile("(?i)\\btypescript\\b"));
                return patterns;
            case "sql":
                patterns.add(Pattern.compile("(?i)\\bsql\\b"));
                return patterns;
            case "junit 5":
                patterns.add(Pattern.compile("(?i)\\b(junit|junit\\s*5|junit5)\\b"));
                return patterns;
            case "selenium":
                patterns.add(Pattern.compile("(?i)\\b(selenium|selenium\\s+webdriver)\\b"));
                return patterns;
            case "agile / scrum":
                patterns.add(Pattern.compile("(?i)\\b(agile|scrum|kanban|sprints)\\b"));
                return patterns;
            case "linux fundamentals":
                patterns.add(Pattern.compile("(?i)\\b(linux|unix|ubuntu|centos|redhat)\\b"));
                return patterns;
            case "bash/shell":
                patterns.add(Pattern.compile("(?i)\\b(bash|shell\\s+scripting|shell\\s+script|zsh)\\b"));
                return patterns;
            default:
                break;
        }

        // Generic boundary-safe pattern for other skills
        if (lowerSkill.contains("+") || lowerSkill.contains("#") || lowerSkill.contains(".")) {
            patterns.add(Pattern.compile("(?i)(?<=^|[^a-zA-Z0-9])" + Pattern.quote(lowerSkill) + "(?=[^a-zA-Z0-9]|$)"));
        } else {
            patterns.add(Pattern.compile("(?i)\\b" + Pattern.quote(lowerSkill) + "\\b"));
        }

        // Add known aliases
        List<String> aliases = getSkillAliases(lowerSkill);
        for (String alias : aliases) {
            patterns.add(Pattern.compile("(?i)\\b" + Pattern.quote(alias) + "\\b"));
        }

        return patterns;
    }

    private List<String> getSkillAliases(String skill) {
        return switch (skill) {
            case "react" -> List.of("reactjs", "react.js");
            case "node.js" -> List.of("nodejs", "node js");
            case "spring boot" -> List.of("springboot", "spring-boot");
            case "postgresql" -> List.of("postgres", "psql");
            case "docker" -> List.of("containerization", "dockerfile");
            case "kubernetes basics" -> List.of("k8s", "kubernetes");
            case "javascript" -> List.of("js", "ecmascript");
            case "typescript" -> List.of("ts");
            case "rest apis" -> List.of("restful", "restful apis", "rest apis");
            case "mongodb" -> List.of("mongo");
            case "git" -> List.of("github", "gitlab");
            default -> Collections.emptyList();
        };
    }

    private static class SkillExtractionResult {
        final boolean matched;
        final SkillProficiency estimatedProficiency;
        final double confidence;
        final String evidenceSnippet;
        final String sectionName;
        final String evidenceType;
        final String reasoning;
        final boolean needsReview;

        SkillExtractionResult(boolean matched, SkillProficiency estimatedProficiency, double confidence,
                              String evidenceSnippet, String sectionName, String evidenceType, String reasoning, boolean needsReview) {
            this.matched = matched;
            this.estimatedProficiency = estimatedProficiency;
            this.confidence = confidence;
            this.evidenceSnippet = evidenceSnippet;
            this.sectionName = sectionName;
            this.evidenceType = evidenceType;
            this.reasoning = reasoning;
            this.needsReview = needsReview;
        }
    }
}
