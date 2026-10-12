package com.skillforge.service;

import com.skillforge.dto.*;
import com.skillforge.exception.ResourceNotFoundException;
import com.skillforge.model.*;
import com.skillforge.repository.SkillRepository;
import com.skillforge.repository.UserSkillRepository;
import org.apache.tika.Tika;
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

    private final SkillRepository skillRepository;
    private final UserSkillRepository userSkillRepository;
    private final ReadinessScoreService readinessScoreService;
    private final Tika tika;

    // Advanced proficiency keywords
    private static final Set<String> ADVANCED_KEYWORDS = Set.of(
            "expert", "architect", "lead", "advanced", "senior", "mastery", "production",
            "scalable", "optimization", "in-depth", "5+ years", "4+ years", "extensive"
    );

    // Intermediate proficiency keywords
    private static final Set<String> INTERMEDIATE_KEYWORDS = Set.of(
            "proficient", "intermediate", "developed", "built", "implemented", "designed",
            "hands-on", "experience", "created", "maintained", "working knowledge", "2+ years", "3+ years"
    );

    public ResumeParserService(
            SkillRepository skillRepository,
            UserSkillRepository userSkillRepository,
            ReadinessScoreService readinessScoreService) {
        this.skillRepository = skillRepository;
        this.userSkillRepository = userSkillRepository;
        this.readinessScoreService = readinessScoreService;
        this.tika = new Tika();
    }

    public ResumeParseResponse parseResume(MultipartFile file, User user) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Resume file must not be empty");
        }

        String originalFilename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "resume.pdf";
        String extractedText;

        try (InputStream is = file.getInputStream()) {
            extractedText = tika.parseToString(is);
        } catch (Exception ex) {
            log.error("Failed to extract text from resume: {}", ex.getMessage(), ex);
            throw new RuntimeException("Could not extract text from the uploaded document. Ensure it is a valid PDF or Word document.", ex);
        }

        if (extractedText == null || extractedText.isBlank()) {
            throw new IllegalArgumentException("No readable text could be extracted from this document.");
        }

        // Clean text for search
        String cleanedText = extractedText.replaceAll("\\r?\\n", " ").replaceAll("\\s+", " ");
        String lowerText = cleanedText.toLowerCase();

        // Load catalog skills and user's current profile skills
        List<Skill> catalogSkills = skillRepository.findAll();
        Set<Integer> existingSkillIds = userSkillRepository.findByUser(user).stream()
                .map(us -> us.getSkill().getSkillId())
                .collect(Collectors.toSet());

        List<ParsedSkillDTO> detectedSkills = new ArrayList<>();

        for (Skill skill : catalogSkills) {
            String skillName = skill.getSkillName().trim();
            SkillMatch match = findSkillMatch(lowerText, cleanedText, skillName);

            if (match.matched) {
                boolean alreadyInProfile = existingSkillIds.contains(skill.getSkillId());
                ParsedSkillDTO dto = new ParsedSkillDTO(
                        skill.getSkillId(),
                        skill.getSkillName(),
                        skill.getCategory(),
                        match.estimatedProficiency,
                        match.confidence,
                        match.snippet,
                        alreadyInProfile
                );
                detectedSkills.add(dto);
            }
        }

        // Sort detected skills: unacquired first, then higher confidence, then category
        detectedSkills.sort((a, b) -> {
            if (a.isAlreadyInProfile() != b.isAlreadyInProfile()) {
                return a.isAlreadyInProfile() ? 1 : -1;
            }
            int conf = Double.compare(b.getConfidence(), a.getConfidence());
            if (conf != 0) return conf;
            return a.getSkillName().compareToIgnoreCase(b.getSkillName());
        });

        String snippet = cleanedText.length() > 300 ? cleanedText.substring(0, 300) + "..." : cleanedText;

        return new ResumeParseResponse(
                originalFilename,
                file.getContentType(),
                extractedText.length(),
                snippet,
                detectedSkills
        );
    }

    @Transactional
    public Map<String, Object> applyResumeSkills(User user, List<ApplyResumeSkillsRequest.SkillItem> skillItems) {
        if (skillItems == null || skillItems.isEmpty()) {
            throw new IllegalArgumentException("No skills provided to add.");
        }

        int addedCount = 0;
        int updatedCount = 0;

        for (ApplyResumeSkillsRequest.SkillItem item : skillItems) {
            if (item.getSkillId() == null) continue;

            Skill skill = skillRepository.findById(item.getSkillId())
                    .orElseThrow(() -> new ResourceNotFoundException("Skill not found: " + item.getSkillId()));

            SkillProficiency proficiency = item.getProficiency() != null ? item.getProficiency() : SkillProficiency.BEGINNER;

            Optional<UserSkill> existing = userSkillRepository.findByUserAndSkill(user, skill);
            if (existing.isPresent()) {
                UserSkill us = existing.get();
                us.setProficiency(proficiency);
                userSkillRepository.save(us);
                updatedCount++;
            } else {
                UserSkill us = new UserSkill();
                us.setUser(user);
                us.setSkill(skill);
                us.setProficiency(proficiency);
                userSkillRepository.save(us);
                addedCount++;
            }
        }

        ReadinessScoreDTO readiness = readinessScoreService.calculateReadiness(user);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("addedCount", addedCount);
        result.put("updatedCount", updatedCount);
        result.put("totalProcessed", addedCount + updatedCount);
        result.put("readiness", readiness);
        result.put("message", String.format("Added %d new competencies and updated %d existing competencies from resume.", addedCount, updatedCount));
        return result;
    }

    private SkillMatch findSkillMatch(String lowerText, String originalText, String skillName) {
        String lowerSkill = skillName.toLowerCase();

        // 1. Exact or boundary-safe pattern matching
        Pattern pattern = buildPatternForSkill(lowerSkill);
        Matcher matcher = pattern.matcher(lowerText);

        if (!matcher.find()) {
            // Check common aliases
            List<String> aliases = getSkillAliases(lowerSkill);
            for (String alias : aliases) {
                Pattern aliasPattern = buildPatternForSkill(alias);
                Matcher aliasMatcher = aliasPattern.matcher(lowerText);
                if (aliasMatcher.find()) {
                    return buildMatch(aliasMatcher, originalText, 0.90);
                }
            }
            return new SkillMatch(false, null, 0.0, null);
        }

        return buildMatch(matcher, originalText, 0.96);
    }

    private SkillMatch buildMatch(Matcher matcher, String originalText, double baseConfidence) {
        int start = matcher.start();
        int end = matcher.end();

        // Extract surrounding context snippet (up to 70 chars before and after)
        int snippetStart = Math.max(0, start - 65);
        int snippetEnd = Math.min(originalText.length(), end + 65);
        String snippet = originalText.substring(snippetStart, snippetEnd).trim();
        if (snippetStart > 0) snippet = "..." + snippet;
        if (snippetEnd < originalText.length()) snippet = snippet + "...";

        // Examine context for proficiency estimation
        String contextLower = snippet.toLowerCase();
        SkillProficiency estimatedProficiency = SkillProficiency.BEGINNER;
        double confidence = baseConfidence;

        for (String kw : ADVANCED_KEYWORDS) {
            if (contextLower.contains(kw)) {
                estimatedProficiency = SkillProficiency.ADVANCED;
                confidence = Math.min(0.99, confidence + 0.03);
                break;
            }
        }

        if (estimatedProficiency == SkillProficiency.BEGINNER) {
            for (String kw : INTERMEDIATE_KEYWORDS) {
                if (contextLower.contains(kw)) {
                    estimatedProficiency = SkillProficiency.INTERMEDIATE;
                    confidence = Math.min(0.98, confidence + 0.02);
                    break;
                }
            }
        }

        // If no keyword matched, but skill clearly appears in technical section, default to INTERMEDIATE
        if (estimatedProficiency == SkillProficiency.BEGINNER && (contextLower.contains("skills") || contextLower.contains("technologies") || contextLower.contains("stack"))) {
            estimatedProficiency = SkillProficiency.INTERMEDIATE;
        }

        return new SkillMatch(true, estimatedProficiency, confidence, snippet);
    }

    private Pattern buildPatternForSkill(String skill) {
        // Handle skills with symbols such as C++, C#, .NET, Node.js
        if (skill.contains("+") || skill.contains("#") || skill.contains(".")) {
            String escaped = Pattern.quote(skill);
            return Pattern.compile("(?i)(?<=^|[^a-zA-Z0-9])" + escaped + "(?=[^a-zA-Z0-9]|$)");
        }
        return Pattern.compile("(?i)\\b" + Pattern.quote(skill) + "\\b");
    }

    private List<String> getSkillAliases(String skill) {
        return switch (skill) {
            case "react" -> List.of("reactjs", "react.js");
            case "node.js" -> List.of("nodejs", "node js");
            case "spring boot" -> List.of("springboot", "spring-boot");
            case "postgresql" -> List.of("postgres", "psql");
            case "docker" -> List.of("containerization", "dockerfile");
            case "kubernetes" -> List.of("k8s");
            case "javascript" -> List.of("js", "ecmascript");
            case "typescript" -> List.of("ts");
            case "aws" -> List.of("amazon web services");
            case "azure" -> List.of("microsoft azure");
            case "gcp" -> List.of("google cloud platform", "google cloud");
            case "rest api" -> List.of("restful", "restful apis", "rest apis");
            case "graphql" -> List.of("graph-ql");
            case "mongodb" -> List.of("mongo");
            case "git" -> List.of("github", "gitlab");
            default -> Collections.emptyList();
        };
    }

    private static class SkillMatch {
        final boolean matched;
        final SkillProficiency estimatedProficiency;
        final double confidence;
        final String snippet;

        SkillMatch(boolean matched, SkillProficiency estimatedProficiency, double confidence, String snippet) {
            this.matched = matched;
            this.estimatedProficiency = estimatedProficiency;
            this.confidence = confidence;
            this.snippet = snippet;
        }
    }
}
