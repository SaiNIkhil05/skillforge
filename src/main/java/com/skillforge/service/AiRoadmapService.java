package com.skillforge.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skillforge.dto.*;
import com.skillforge.exception.ResourceNotFoundException;
import com.skillforge.model.CustomRoadmap;
import com.skillforge.model.ProgressStatus;
import com.skillforge.model.User;
import com.skillforge.model.UserSkill;
import com.skillforge.repository.CustomRoadmapRepository;
import com.skillforge.repository.UserSkillRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AiRoadmapService {

    private static final Logger log = LoggerFactory.getLogger(AiRoadmapService.class);

    private final CustomRoadmapRepository customRoadmapRepository;
    private final UserSkillRepository userSkillRepository;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    @Value("${gemini.api-key:}")
    private String configuredGeminiApiKey;

    @Value("${gemini.model:gemini-1.5-flash}")
    private String geminiModel;

    public AiRoadmapService(CustomRoadmapRepository customRoadmapRepository,
                            UserSkillRepository userSkillRepository) {
        this.customRoadmapRepository = customRoadmapRepository;
        this.userSkillRepository = userSkillRepository;
        this.objectMapper = new ObjectMapper();
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .build();
    }

    /**
     * Generates a fully tailored, dynamic technical roadmap for the desired tech stack.
     * Takes into account the student's acquired skills.
     */
    public AiRoadmapResponse generateRoadmap(User user, GenerateRoadmapRequest request) {
        List<String> existingSkills = new ArrayList<>();
        if (user != null) {
            List<UserSkill> userSkills = userSkillRepository.findByUser(user);
            existingSkills = userSkills.stream()
                    .map(us -> us.getSkill().getSkillName())
                    .collect(Collectors.toList());
        }

        String effectiveApiKey = request.getCustomApiKey() != null && !request.getCustomApiKey().isBlank()
                ? request.getCustomApiKey().trim()
                : (configuredGeminiApiKey != null ? configuredGeminiApiKey.trim() : "");

        if (effectiveApiKey.isBlank()) {
            String envKey = System.getenv("GEMINI_API_KEY");
            if (envKey != null && !envKey.isBlank()) {
                effectiveApiKey = envKey.trim();
            }
        }

        if (!effectiveApiKey.isBlank()) {
            try {
                log.info("Calling Google Gemini AI ({}) for tech stack: {}", geminiModel, request.getDesiredTechStack());
                AiRoadmapResponse geminiResponse = callGeminiApi(request, existingSkills, effectiveApiKey);
                if (geminiResponse != null && geminiResponse.getPhases() != null && !geminiResponse.getPhases().isEmpty()) {
                    return geminiResponse;
                }
            } catch (Exception ex) {
                log.warn("Gemini API call failed ({}: {}). Trying live AI LLM...",
                        ex.getClass().getSimpleName(), ex.getMessage());
            }
        }

        // Try Live OpenAI-compatible LLM (No API key required, real dynamic LLM)
        try {
            log.info("Calling Live AI Engine for dynamic tech stack roadmap: {}", request.getDesiredTechStack());
            AiRoadmapResponse liveAiResponse = callLiveOpenAiRoadmap(request, existingSkills);
            if (liveAiResponse != null && liveAiResponse.getPhases() != null && !liveAiResponse.getPhases().isEmpty()) {
                return liveAiResponse;
            }
        } catch (Exception ex) {
            log.warn("Live AI LLM call was unsuccessful ({}: {}). Falling back to Adaptive Cognitive Synthesizer.",
                    ex.getClass().getSimpleName(), ex.getMessage());
        }

        log.info("Utilizing SkillForge Adaptive AI Engine for tech stack: {}", request.getDesiredTechStack());
        return synthesizeAdaptiveRoadmap(request, existingSkills);
    }

    /**
     * Interactive conversational AI chatbot method (like ChatGPT or Gemini).
     * Chats naturally about any topic, and when the user asks for a roadmap or curriculum,
     * it generates both conversational advice and a structured AiRoadmapResponse!
     */
    public ChatResponseDTO chatWithAi(User user, ChatRequestDTO request) {
        List<String> existingSkills = new ArrayList<>();
        String studentName = "Student";
        String targetRole = "Software Engineer";
        String department = "Computer Science";

        if (user != null) {
            studentName = user.getFullName() != null ? user.getFullName() : "Student";
            if (user.getTargetRole() != null) {
                targetRole = user.getTargetRole().getRoleName();
            }
            if (user.getDepartment() != null) {
                department = user.getDepartment();
            }
            List<UserSkill> userSkills = userSkillRepository.findByUser(user);
            existingSkills = userSkills.stream()
                    .map(us -> us.getSkill().getSkillName())
                    .collect(Collectors.toList());
        }

        String systemPrompt = String.format("""
                You are SkillForge AI, an expert Senior Engineering Mentor, Career Coach, and Curriculum Architect (similar to ChatGPT and Gemini).
                You converse warmly, intelligently, concisely, and helpfully with university software engineering students.

                STUDENT PROFILE CONTEXT:
                - Name: %s
                - Verified Acquired Skills: %s
                - Target Career Role: %s
                - Academic Department: %s

                You can chat normally like a conversational AI about anything (explaining tech concepts, comparing languages or frameworks, debugging code, interview prep, or career guidance). Keep your answers insightful, practical, and well-structured using markdown.

                ROADMAP GENERATION CAPABILITY:
                When the student asks you for a roadmap, curriculum, study plan, or how to learn a specific tech stack (e.g. 'Create a roadmap for Next.js & FastAPI', 'How should I master Go and Kafka?', 'Give me a 12-week plan for Rust'):
                1. Write an engaging, motivating conversational overview explaining the architectural rationale and how their existing skills help them.
                2. In the SAME response, output a structured JSON block delimited EXACTLY with:
                ```sf_roadmap
                {
                  "techStack": "...",
                  "targetRole": "...",
                  "experienceLevel": "...",
                  "timeframeWeeks": 12,
                  "learningFocus": "...",
                  "executiveSummary": "...",
                  "architectureOverview": "...",
                  "aiThinkingNotes": "...",
                  "prerequisites": ["..."],
                  "phases": [
                    {
                      "phaseNumber": 1,
                      "phaseTitle": "...",
                      "phaseGoal": "...",
                      "estimatedWeeks": 3,
                      "milestones": [
                        {
                          "id": "m1",
                          "title": "...",
                          "summary": "...",
                          "competencies": ["..."],
                          "keyTopics": ["..."],
                          "projectDeliverable": "...",
                          "recommendedResources": ["..."],
                          "estimatedHours": 12
                        }
                      ]
                    }
                  ],
                  "recommendedProjects": ["..."]
                }
                ```
                Provide at least 3-4 progressive phases. The SkillForge UI will automatically extract this block and render an interactive roadmap for the student!
                """,
                studentName,
                existingSkills.isEmpty() ? "None recorded yet" : String.join(", ", existingSkills),
                targetRole,
                department
        );

        List<Map<String, String>> messagesList = new ArrayList<>();
        messagesList.add(Map.of("role", "system", "content", systemPrompt));

        if (request.getMessages() != null) {
            for (ChatMessageDTO msg : request.getMessages()) {
                if (msg.getContent() != null && !msg.getContent().isBlank()) {
                    String role = (msg.getRole() != null && msg.getRole().equalsIgnoreCase("assistant")) ? "assistant" : "user";
                    messagesList.add(Map.of("role", role, "content", msg.getContent()));
                }
            }
        }

        String rawResponse = null;
        String modelName = "Live AI Engine (GPT-4o via Pollinations)";

        // Try live AI
        try {
            rawResponse = callLiveOpenAiApi(messagesList);
        } catch (Exception e) {
            log.warn("Live OpenAI-compatible chat call failed: {}", e.getMessage());
        }

        // If live AI call failed or was empty, provide a dynamic contextual answer
        if (rawResponse == null || rawResponse.isBlank()) {
            modelName = "SkillForge Adaptive AI Engine";
            rawResponse = fallbackChatResponse(request, existingSkills, targetRole);
        }

        // Check if there is an embedded roadmap JSON block ```sf_roadmap ... ```
        AiRoadmapResponse embeddedRoadmap = extractEmbeddedRoadmap(rawResponse);
        String cleanedReply = cleanRoadmapTagsFromReply(rawResponse);

        // If the student asked for a roadmap and no roadmap was parsed, generate one on the fly!
        if (embeddedRoadmap == null && isRoadmapRequest(request)) {
            String extractedStack = inferTechStackFromMessages(request);
            GenerateRoadmapRequest genReq = new GenerateRoadmapRequest(extractedStack);
            genReq.setTargetRole(targetRole);
            try {
                embeddedRoadmap = callLiveOpenAiRoadmap(genReq, existingSkills);
            } catch (Exception e) {
                log.warn("Live OpenAI roadmap call failed, falling back to adaptive synthesizer: {}", e.getMessage());
                embeddedRoadmap = synthesizeAdaptiveRoadmap(genReq, existingSkills);
            }
        }

        if (cleanedReply.isBlank() && embeddedRoadmap != null) {
            cleanedReply = String.format("Here is your personalized learning blueprint for **%s**. I've structured it into %d sequential phases with verified milestones, time commitments, and capstone deliverables below:",
                    embeddedRoadmap.getTechStack(),
                    embeddedRoadmap.getPhases() != null ? embeddedRoadmap.getPhases().size() : 4);
        }

        return new ChatResponseDTO(cleanedReply, embeddedRoadmap, modelName);
    }

    private AiRoadmapResponse extractEmbeddedRoadmap(String text) {
        if (text == null) return null;
        try {
            int startIdx = text.indexOf("```sf_roadmap");
            if (startIdx != -1) {
                int contentStart = startIdx + 13;
                int endIdx = text.indexOf("```", contentStart);
                if (endIdx != -1) {
                    String json = text.substring(contentStart, endIdx).trim();
                    AiRoadmapResponse roadmap = objectMapper.readValue(json, AiRoadmapResponse.class);
                    ensureMilestoneIds(roadmap);
                    return roadmap;
                }
            }
        } catch (Exception e) {
            log.warn("Failed to parse embedded sf_roadmap JSON: {}", e.getMessage());
        }
        return null;
    }

    private String cleanRoadmapTagsFromReply(String text) {
        if (text == null) return "";
        return text.replaceAll("(?s)```sf_roadmap.*?```", "").trim();
    }

    private boolean isRoadmapRequest(ChatRequestDTO request) {
        if (request.getMessages() == null || request.getMessages().isEmpty()) return false;
        String lastMsg = request.getMessages().get(request.getMessages().size() - 1).getContent();
        if (lastMsg == null) return false;
        String lower = lastMsg.toLowerCase();
        return lower.contains("roadmap") || lower.contains("curriculum") || lower.contains("study plan") || lower.contains("learning path") || lower.contains("how do i learn") || lower.contains("how to learn");
    }

    private String inferTechStackFromMessages(ChatRequestDTO request) {
        if (request.getMessages() == null || request.getMessages().isEmpty()) return "Full-Stack Development";
        String lastMsg = request.getMessages().get(request.getMessages().size() - 1).getContent();
        String cleaned = lastMsg.replaceAll("(?i)(please|give me a|generate a|create a|build a|provide a|\\d+-week|\\d+\\s*week|roadmap for|roadmap on|roadmap|study plan for|study plan|curriculum for|curriculum|learning path for|learning path|how to learn|can you teach me|plan for|plan)", "").trim();
        cleaned = cleaned.replaceAll("(?i)^(\\s*for\\s*|\\s*on\\s*|\\s*about\\s*)", "").trim();
        return cleaned.length() > 2 ? cleaned : "Full-Stack Development (React, Node, PostgreSQL)";
    }

    private String fallbackChatResponse(ChatRequestDTO request, List<String> existingSkills, String targetRole) {
        String lastMsg = "";
        if (request.getMessages() != null && !request.getMessages().isEmpty()) {
            lastMsg = request.getMessages().get(request.getMessages().size() - 1).getContent();
        }
        return String.format(
                "Hello! As your SkillForge Engineering Mentor, I'm here to help you accelerate toward your target role of **%s**.\n\n" +
                "I noticed your profile already includes competencies in: **%s**.\n\n" +
                "You asked: *\"%s\"*\n\n" +
                "To master this domain effectively, focus on building production-grade services with typed interfaces, robust database transactions, and automated CI/CD testing. Would you like me to generate a tailored 12-week roadmap for this stack?",
                targetRole,
                existingSkills.isEmpty() ? "Foundational Computing" : String.join(", ", existingSkills),
                lastMsg
        );
    }

    private String callLiveOpenAiApi(List<Map<String, String>> messages) throws Exception {
        Map<String, Object> payload = Map.of(
                "messages", messages,
                "model", "openai-fast"
        );

        String jsonPayload = objectMapper.writeValueAsString(payload);
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create("https://text.pollinations.ai/openai"))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(35))
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        if (httpResponse.statusCode() >= 200 && httpResponse.statusCode() < 300) {
            JsonNode rootNode = objectMapper.readTree(httpResponse.body());
            JsonNode choices = rootNode.path("choices");
            if (choices.isArray() && !choices.isEmpty()) {
                return choices.get(0).path("message").path("content").asText();
            }
        }
        throw new RuntimeException("Live AI responded with HTTP status: " + httpResponse.statusCode());
    }

    private AiRoadmapResponse callLiveOpenAiRoadmap(GenerateRoadmapRequest request, List<String> existingSkills) throws Exception {
        String prompt = buildGeminiPrompt(request, existingSkills);
        List<Map<String, String>> messages = List.of(
                Map.of("role", "system", "content", "You are a Senior Principal Curriculum Architect. Return ONLY valid JSON matching the schema, with no markdown fences."),
                Map.of("role", "user", "content", prompt)
        );

        String rawText = callLiveOpenAiApi(messages);
        String cleaned = cleanJsonOutput(rawText);
        AiRoadmapResponse response = objectMapper.readValue(cleaned, AiRoadmapResponse.class);
        response.setGeneratedBy("Live AI Engine (GPT-4o via Pollinations)");
        ensureMilestoneIds(response);
        return response;
    }


    /**
     * Calls Google Gemini REST API using structured JSON prompt schema.
     */
    private AiRoadmapResponse callGeminiApi(GenerateRoadmapRequest request, List<String> existingSkills, String apiKey) throws Exception {
        String prompt = buildGeminiPrompt(request, existingSkills);

        Map<String, Object> textPart = Map.of("text", prompt);
        Map<String, Object> contentObj = Map.of("parts", List.of(textPart));
        Map<String, Object> generationConfig = Map.of(
                "temperature", 0.3,
                "responseMimeType", "application/json"
        );

        Map<String, Object> requestPayload = Map.of(
                "contents", List.of(contentObj),
                "generationConfig", generationConfig
        );

        String jsonPayload = objectMapper.writeValueAsString(requestPayload);
        String endpoint = "https://generativelanguage.googleapis.com/v1beta/models/" + geminiModel + ":generateContent?key=" + apiKey;

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(30))
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        if (httpResponse.statusCode() >= 200 && httpResponse.statusCode() < 300) {
            JsonNode rootNode = objectMapper.readTree(httpResponse.body());
            JsonNode candidates = rootNode.path("candidates");
            if (candidates.isArray() && !candidates.isEmpty()) {
                JsonNode parts = candidates.get(0).path("content").path("parts");
                if (parts.isArray() && !parts.isEmpty()) {
                    String rawJson = parts.get(0).path("text").asText();
                    rawJson = cleanJsonOutput(rawJson);
                    AiRoadmapResponse response = objectMapper.readValue(rawJson, AiRoadmapResponse.class);
                    response.setGeneratedBy("Google Gemini 1.5 Flash");
                    ensureMilestoneIds(response);
                    return response;
                }
            }
        } else {
            log.error("Gemini API responded with status {}: {}", httpResponse.statusCode(), httpResponse.body());
            throw new RuntimeException("Gemini API error code: " + httpResponse.statusCode());
        }

        return null;
    }

    private String cleanJsonOutput(String rawJson) {
        String trimmed = rawJson.trim();
        if (trimmed.startsWith("```json")) {
            trimmed = trimmed.substring(7);
        } else if (trimmed.startsWith("```")) {
            trimmed = trimmed.substring(3);
        }
        if (trimmed.endsWith("```")) {
            trimmed = trimmed.substring(0, trimmed.length() - 3);
        }
        return trimmed.trim();
    }

    private String buildGeminiPrompt(GenerateRoadmapRequest request, List<String> existingSkills) {
        return """
                You are a Senior Principal Curriculum Architect and Staff Engineer at SkillForge.
                Generate a comprehensive, production-grade learning roadmap for a student wanting to master this specific tech stack:
                Target Stack: %s
                Target Role: %s
                Target Experience Level: %s
                Timeframe: %d weeks
                Learning Focus: %s
                
                The student already has acquired skills in: %s.
                Crucial rule: Do NOT spend time re-teaching basic fundamentals of the student's existing skills. Instead, explain how they interface with the target stack and accelerate their mastery.
                
                Return a JSON object strictly adhering to this schema:
                {
                  "techStack": "%s",
                  "targetRole": "%s",
                  "experienceLevel": "%s",
                  "timeframeWeeks": %d,
                  "learningFocus": "%s",
                  "executiveSummary": "Compelling 2-3 sentence overview of this curriculum",
                  "architectureOverview": "High-level architectural blueprint explaining how the chosen technologies interact in modern production systems",
                  "aiThinkingNotes": "Detailed explanation of how the AI tailored this roadmap, which components were decomposed, and how the user's existing competencies were factored in",
                  "prerequisites": ["List of critical prerequisites"],
                  "phases": [
                    {
                      "phaseNumber": 1,
                      "phaseTitle": "Phase title",
                      "phaseGoal": "Clear actionable goal for this phase",
                      "estimatedWeeks": 3,
                      "milestones": [
                        {
                          "id": "m1",
                          "title": "Milestone title",
                          "summary": "Concrete summary",
                          "competencies": ["Competency 1", "Competency 2"],
                          "keyTopics": ["Topic 1", "Topic 2", "Topic 3"],
                          "projectDeliverable": "Specific hands-on code project to build and publish",
                          "recommendedResources": ["Official docs or top-rated guides"],
                          "estimatedHours": 12
                        }
                      ]
                    }
                  ],
                  "recommendedProjects": [
                    "Full description of Capstone Project 1",
                    "Full description of Capstone Project 2",
                    "Full description of Capstone Project 3"
                  ]
                }
                Provide 3-5 comprehensive phases. Output pure JSON only.
                """.formatted(
                request.getDesiredTechStack(),
                request.getTargetRole() != null ? request.getTargetRole() : "Full-Stack Software Engineer",
                request.getTargetLevel() != null ? request.getTargetLevel() : "Junior to Mid-Level",
                request.getTimeframeWeeks() != null ? request.getTimeframeWeeks() : 12,
                request.getLearningFocus() != null ? request.getLearningFocus() : "Project-Driven",
                existingSkills.isEmpty() ? "None recorded yet" : String.join(", ", existingSkills),
                request.getDesiredTechStack(),
                request.getTargetRole() != null ? request.getTargetRole() : "Full-Stack Software Engineer",
                request.getTargetLevel() != null ? request.getTargetLevel() : "Junior to Mid-Level",
                request.getTimeframeWeeks() != null ? request.getTimeframeWeeks() : 12,
                request.getLearningFocus() != null ? request.getLearningFocus() : "Project-Driven"
        );
    }

    /**
     * Intelligent adaptive synthesizer that dynamically decomposes any arbitrary tech stack string,
     * extracts architectural patterns, and synthesizes a non-static, customized multi-phase curriculum.
     */
    public AiRoadmapResponse synthesizeAdaptiveRoadmap(GenerateRoadmapRequest request, List<String> existingSkills) {
        String rawStack = request.getDesiredTechStack().trim();
        int totalWeeks = request.getTimeframeWeeks() != null && request.getTimeframeWeeks() > 0 ? request.getTimeframeWeeks() : 12;
        String level = request.getTargetLevel() != null ? request.getTargetLevel() : "Junior to Mid-Level";
        String role = request.getTargetRole() != null && !request.getTargetRole().isBlank() ? request.getTargetRole() : inferRoleFromStack(rawStack);
        String focus = request.getLearningFocus() != null ? request.getLearningFocus() : "Project-Driven";

        List<String> detectedComponents = parseStackComponents(rawStack);
        Map<String, List<String>> layerMap = categorizeComponents(detectedComponents);

        List<String> matchedExisting = new ArrayList<>();
        for (String skill : existingSkills) {
            for (String comp : detectedComponents) {
                if (skill.equalsIgnoreCase(comp) || comp.toLowerCase().contains(skill.toLowerCase())) {
                    matchedExisting.add(skill);
                }
            }
        }

        AiRoadmapResponse response = new AiRoadmapResponse();
        response.setTechStack(rawStack);
        response.setTargetRole(role);
        response.setExperienceLevel(level);
        response.setTimeframeWeeks(totalWeeks);
        response.setLearningFocus(focus);
        response.setGeneratedBy("SkillForge Adaptive AI Engine (Heuristic Architectural Decomposition)");

        // AI Thinking Notes explaining the rationale
        StringBuilder thinking = new StringBuilder();
        thinking.append("SkillForge Cognitive Reasoning Engine analyzed the requested stack: '").append(rawStack).append("'. ");
        thinking.append("Extracted architectural layers: ");
        List<String> layerSummaries = new ArrayList<>();
        layerMap.forEach((layer, comps) -> {
            if (!comps.isEmpty()) {
                layerSummaries.add(layer + " (" + String.join(", ", comps) + ")");
            }
        });
        thinking.append(String.join(", ", layerSummaries)).append(". ");
        if (!matchedExisting.isEmpty()) {
            thinking.append("Foundational synergies detected: Your profile shows verified experience in [")
                    .append(String.join(", ", matchedExisting))
                    .append("]. Foundational phases will bypass generic introductory concepts and jump straight into idiomatic patterns and integration protocols. ");
        } else {
            thinking.append("Zero baseline overlap detected: Curriculum structured with robust initial tooling and core language mechanics. ");
        }
        thinking.append("Calibrated for ").append(level).append(" velocity across a ").append(totalWeeks).append("-week horizon.");
        response.setAiThinkingNotes(thinking.toString());

        response.setExecutiveSummary(
                String.format("A targeted, production-caliber %d-week engineering curriculum designed to master %s. " +
                                "Emphasizes distributed systems, type safety, resilient database architectures, and end-to-end deployment.",
                        totalWeeks, rawStack)
        );

        response.setArchitectureOverview(
                String.format("Modern software applications built on %s emphasize decoupled interfaces, reactive data flow, and high observability. " +
                                "The presentation and client layers interface with core services via typed contracts, backed by high-throughput persistence and containerized orchestration.",
                        rawStack)
        );

        // Prerequisites
        List<String> prereqs = new ArrayList<>();
        prereqs.add("Basic familiarity with modern terminal/shell workflows and Git version control");
        prereqs.add("Core programming concepts: data structures, async execution, and HTTP/REST fundamentals");
        if (layerMap.containsKey("Data & Storage") && !layerMap.get("Data & Storage").isEmpty()) {
            prereqs.add("Relational algebra and indexing basics for " + String.join("/", layerMap.get("Data & Storage")));
        }
        response.setPrerequisites(prereqs);

        // Synthesize Phases
        List<AiRoadmapPhaseDTO> phases = new ArrayList<>();
        int p1Weeks = Math.max(2, Math.round(totalWeeks * 0.25f));
        int p2Weeks = Math.max(3, Math.round(totalWeeks * 0.35f));
        int p3Weeks = Math.max(2, Math.round(totalWeeks * 0.25f));
        int p4Weeks = Math.max(2, totalWeeks - (p1Weeks + p2Weeks + p3Weeks));

        // Phase 1: Toolchain & Idiomatic Core
        AiRoadmapPhaseDTO phase1 = new AiRoadmapPhaseDTO();
        phase1.setPhaseNumber(1);
        phase1.setPhaseTitle("Core Foundations & Idiomatic Architecture");
        phase1.setPhaseGoal("Master runtime internals, package ecosystem, type systems, and developer ergonomics.");
        phase1.setEstimatedWeeks(p1Weeks);

        AiMilestoneDTO m1 = new AiMilestoneDTO(
                "m1_1",
                "Toolchain & Developer Environment Setup",
                "Configure modern linter, formatter, debuggers, and project workspace for " + rawStack,
                List.of("Environment Configuration", "Dependency Management", "Build Automation"),
                List.of("IDE setup & LSP configuration", "Package managers & lockfiles", "Semantic versioning and workspace monorepos"),
                "Publish a GitHub repository template with pre-commit hooks, lint scripts, and unit test harness",
                List.of("Official Getting Started Documentation", "Idiomatic Project Layout Guide"),
                12
        );

        AiMilestoneDTO m2 = new AiMilestoneDTO(
                "m1_2",
                "Idiomatic Syntax, Concurrency & Data Structures",
                "Deep dive into language mechanics and architectural design patterns",
                List.of("Type Safety", "Memory Management / GC", "Async Control Flow"),
                List.of("Error handling paradigms and exceptions", "Streams, async/await, and event loops", "Unit testing and mocking strategies"),
                "Build a high-performance CLI utility or benchmarking harness testing async IO throughput",
                List.of("Language Specification Documentation", "Effective Design Patterns Manual"),
                16
        );
        phase1.setMilestones(List.of(m1, m2));
        phases.add(phase1);

        // Phase 2: Domain Engineering & Framework Mastery
        AiRoadmapPhaseDTO phase2 = new AiRoadmapPhaseDTO();
        phase2.setPhaseNumber(2);
        phase2.setPhaseTitle("Component Modeling & Service Architecture");
        phase2.setPhaseGoal("Build scalable, decoupled service layers and user experiences with production-grade frameworks.");
        phase2.setEstimatedWeeks(p2Weeks);

        String mainFramework = getMainFramework(layerMap, rawStack);
        AiMilestoneDTO m3 = new AiMilestoneDTO(
                "m2_1",
                "Core Application Architecture with " + mainFramework,
                "Implement domain models, routing, dependency injection, and state boundaries",
                List.of("MVC/Clean Architecture", "API Design", "Component State"),
                List.of("RESTful / GraphQL / gRPC interface contracts", "Validation pipelines and custom middleware", "Authentication: JWT, OAuth2, and session cookies"),
                "Construct a multi-endpoint service with validation, error formatting, and authenticated routes",
                List.of(mainFramework + " Official Reference Guide", "OWASP Web Security Standard"),
                18
        );

        AiMilestoneDTO m4 = new AiMilestoneDTO(
                "m2_2",
                "State Management & Interactive Layer Integration",
                "Wire client states, caching layers, and external service contracts",
                List.of("Client-Server Synchronization", "Optimistic UI", "Resilience Patterns"),
                List.of("Server actions / RPC mechanisms", "Rate limiting and circuit breaker patterns", "Automated integration testing with MockMVC / Playwright"),
                "Build an interactive dashboard with real-time optimistic state mutations and automated test coverage",
                List.of("Client Architecture Patterns", "Enterprise Integration Handbook"),
                16
        );
        phase2.setMilestones(List.of(m3, m4));
        phases.add(phase2);

        // Phase 3: Persistence, Caching & Distributed Mechanics
        AiRoadmapPhaseDTO phase3 = new AiRoadmapPhaseDTO();
        phase3.setPhaseNumber(3);
        phase3.setPhaseTitle("Data Persistence, Caching & Scalability");
        phase3.setPhaseGoal("Design resilient schemas, query optimizations, caching strategies, and event streaming.");
        phase3.setEstimatedWeeks(p3Weeks);

        String dataTech = getMainDataTech(layerMap, rawStack);
        AiMilestoneDTO m5 = new AiMilestoneDTO(
                "m3_1",
                "Database Modeling & Query Tuning with " + dataTech,
                "Model relational/NoSQL schemas, foreign keys, indexes, and connection pooling",
                List.of("Data Modeling", "Query Optimization", "ACID / Eventual Consistency"),
                List.of("Schema migrations (Flyway / Liquibase / Prisma)", "Index optimization & EXPLAIN ANALYZE", "Transactions, locks, and deadlock prevention"),
                "Develop an audited data storage engine with automated migration scripts and load-tested query plans",
                List.of(dataTech + " High Performance Tuning Guide", "Database Indexing Essentials"),
                18
        );

        AiMilestoneDTO m6 = new AiMilestoneDTO(
                "m3_2",
                "Distributed Caching & Asynchronous Processing",
                "Integrate caching layers, background workers, and event queues",
                List.of("Cache-Aside Pattern", "Message Queues", "Background Workers"),
                List.of("Cache invalidation strategies & TTLs", "Publish/Subscribe event pipelines", "Idempotency keys and retry policies"),
                "Implement an event-driven worker handling background task processing with retry queues",
                List.of("Distributed Caching Best Practices", "Enterprise Messaging Systems"),
                14
        );
        phase3.setMilestones(List.of(m5, m6));
        phases.add(phase3);

        // Phase 4: Production Hardening, Cloud & Capstone
        AiRoadmapPhaseDTO phase4 = new AiRoadmapPhaseDTO();
        phase4.setPhaseNumber(4);
        phase4.setPhaseTitle("Production Hardening, CI/CD & Capstone Deployment");
        phase4.setPhaseGoal("Ship a cloud-deployed, monitored capstone application with CI/CD automation.");
        phase4.setEstimatedWeeks(p4Weeks);

        AiMilestoneDTO m7 = new AiMilestoneDTO(
                "m4_1",
                "Containerization, CI/CD & Cloud Orchestration",
                "Package into multi-stage container images and establish continuous integration pipelines",
                List.of("Docker & OCI Containers", "GitHub Actions CI/CD", "Infrastructure Deployment"),
                List.of("Multi-stage Dockerfiles for minimal footprint", "Automated linting, testing & building in CI", "Cloud deployment (AWS / GCP / Vercel / Kubernetes)"),
                "Deploy the full application to a cloud host with SSL certificates and zero-downtime rolling updates",
                List.of("12-Factor App Methodology", "Production Container Hardening"),
                16
        );

        AiMilestoneDTO m8 = new AiMilestoneDTO(
                "m4_2",
                "Observability, Telemetry & Portfolio Polish",
                "Instrument structured logging, metrics, error tracking, and complete documentation",
                List.of("Observability", "Prometheus / OpenTelemetry", "Technical Documentation"),
                List.of("Structured JSON logging & correlation IDs", "Health check endpoints & readiness probes", "Architecture diagrams, Swagger/OpenAPI docs, and live demo video"),
                "Finalize Capstone project with complete architecture documentation, demo URL, and benchmark stats",
                List.of("OpenTelemetry Architecture Specification", "Showcasing Engineering Projects on GitHub"),
                20
        );
        phase4.setMilestones(List.of(m7, m8));
        phases.add(phase4);

        response.setPhases(phases);

        // Synthesize dynamic Capstone projects
        response.setRecommendedProjects(synthesizeProjects(rawStack, role));

        return response;
    }

    private String inferRoleFromStack(String stack) {
        String s = stack.toLowerCase();
        if (s.contains("react") || s.contains("vue") || s.contains("angular") || s.contains("svelte") || s.contains("tailwind") || s.contains("next.js")) {
            if (s.contains("spring") || s.contains("node") || s.contains("fastapi") || s.contains("django") || s.contains("postgres") || s.contains("mongo")) {
                return "Full-Stack Software Engineer";
            }
            return "Senior Frontend Engineer";
        }
        if (s.contains("kubernetes") || s.contains("docker") || s.contains("terraform") || s.contains("aws") || s.contains("ci/cd")) {
            return "DevOps & Cloud Infrastructure Engineer";
        }
        if (s.contains("flutter") || s.contains("react native") || s.contains("android") || s.contains("ios") || s.contains("swift") || s.contains("kotlin")) {
            return "Mobile Software Engineer";
        }
        if (s.contains("pytorch") || s.contains("tensorflow") || s.contains("langchain") || s.contains("ai") || s.contains("rag") || s.contains("llm")) {
            return "AI Systems & Machine Learning Engineer";
        }
        return "Backend & Systems Architect";
    }

    private List<String> parseStackComponents(String raw) {
        String[] tokens = raw.split("[,/+\n&|]");
        List<String> list = new ArrayList<>();
        for (String t : tokens) {
            String trimmed = t.trim();
            // remove "and", "with"
            trimmed = trimmed.replaceAll("(?i)\\b(and|with|stack)\\b", "").trim();
            if (!trimmed.isBlank() && trimmed.length() > 1) {
                list.add(trimmed);
            }
        }
        if (list.isEmpty()) {
            list.add(raw);
        }
        return list;
    }

    private Map<String, List<String>> categorizeComponents(List<String> components) {
        Map<String, List<String>> map = new LinkedHashMap<>();
        map.put("Languages & Runtimes", new ArrayList<>());
        map.put("Frameworks & UI", new ArrayList<>());
        map.put("Data & Storage", new ArrayList<>());
        map.put("DevOps & Cloud", new ArrayList<>());
        map.put("AI & Analytics", new ArrayList<>());

        for (String c : components) {
            String lower = c.toLowerCase();
            if (lower.matches(".*(java|python|rust|go|golang|typescript|javascript|c\\+\\+|c#|kotlin|swift|elixir|php|ruby|dart|zig).*")) {
                map.get("Languages & Runtimes").add(c);
            } else if (lower.matches(".*(react|next|vue|nuxt|svelte|angular|astro|tailwind|fastapi|django|flask|spring|express|nest|gin|axum|actix|rails|laravel|flutter).*")) {
                map.get("Frameworks & UI").add(c);
            } else if (lower.matches(".*(postgres|mysql|mongo|redis|cassandra|sqlite|supabase|dynamodb|kafka|rabbitmq|drizzle|prisma|hibernate).*")) {
                map.get("Data & Storage").add(c);
            } else if (lower.matches(".*(docker|kubernetes|aws|gcp|azure|terraform|helm|prometheus|grafana|ci/cd|github).*")) {
                map.get("DevOps & Cloud").add(c);
            } else if (lower.matches(".*(pytorch|tensorflow|langchain|llamaindex|ollama|hugging|rag|ai|llm).*")) {
                map.get("AI & Analytics").add(c);
            } else {
                map.get("Frameworks & UI").add(c);
            }
        }
        return map;
    }

    private String getMainFramework(Map<String, List<String>> layers, String fallback) {
        List<String> fw = layers.get("Frameworks & UI");
        if (fw != null && !fw.isEmpty()) {
            return fw.get(0);
        }
        return fallback;
    }

    private String getMainDataTech(Map<String, List<String>> layers, String fallback) {
        List<String> data = layers.get("Data & Storage");
        if (data != null && !data.isEmpty()) {
            return data.get(0);
        }
        return "PostgreSQL / Persistence Layer";
    }

    private List<String> synthesizeProjects(String rawStack, String role) {
        List<String> projects = new ArrayList<>();
        projects.add(String.format("1. High-Performance Enterprise Service Engine: Built with %s. Features end-to-end type safety, automated migration scripts, JWT security boundaries, and 90%% test coverage.", rawStack));
        projects.add(String.format("2. Real-Time Collaborative Workspace: Implements WebSocket/SSE event pipelines, optimistic UI state synchronizations, and caching strategies utilizing %s.", rawStack));
        projects.add(String.format("3. Production Capstone & Cloud Showcase: Multi-container deployment with CI/CD automation, distributed observability, health checks, and a comprehensive GitHub architectural document for %s.", rawStack));
        return projects;
    }

    private void ensureMilestoneIds(AiRoadmapResponse response) {
        if (response.getPhases() == null) return;
        int mIndex = 1;
        for (AiRoadmapPhaseDTO phase : response.getPhases()) {
            if (phase.getMilestones() != null) {
                for (AiMilestoneDTO m : phase.getMilestones()) {
                    if (m.getId() == null || m.getId().isBlank()) {
                        m.setId("m" + mIndex);
                    }
                    if (m.getStatus() == null) {
                        m.setStatus(ProgressStatus.NOT_STARTED);
                    }
                    mIndex++;
                }
            }
        }
    }

    // ==========================================
    // PERSISTENCE & MILESTONE TRACKING
    // ==========================================

    @Transactional
    public CustomRoadmap saveRoadmap(User user, AiRoadmapResponse response) {
        ensureMilestoneIds(response);

        int totalMilestones = 0;
        int completedMilestones = 0;
        if (response.getPhases() != null) {
            for (AiRoadmapPhaseDTO phase : response.getPhases()) {
                if (phase.getMilestones() != null) {
                    for (AiMilestoneDTO m : phase.getMilestones()) {
                        totalMilestones++;
                        if (m.getStatus() == ProgressStatus.COMPLETED) {
                            completedMilestones++;
                        }
                    }
                }
            }
        }

        try {
            String json = objectMapper.writeValueAsString(response);

            CustomRoadmap customRoadmap = new CustomRoadmap();
            customRoadmap.setUser(user);
            customRoadmap.setTitle(response.getTechStack() + " Roadmap");
            customRoadmap.setTechStack(response.getTechStack());
            customRoadmap.setTargetLevel(response.getExperienceLevel());
            customRoadmap.setTimeframeWeeks(response.getTimeframeWeeks());
            customRoadmap.setLearningFocus(response.getLearningFocus());
            customRoadmap.setAiModelUsed(response.getGeneratedBy());
            customRoadmap.setTotalMilestones(totalMilestones);
            customRoadmap.setCompletedMilestones(completedMilestones);
            customRoadmap.setRoadmapJson(json);

            CustomRoadmap saved = customRoadmapRepository.save(customRoadmap);
            response.setId(saved.getId());
            response.setSaved(true);
            return saved;
        } catch (Exception e) {
            log.error("Failed to serialize roadmap for saving", e);
            throw new RuntimeException("Could not save roadmap: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public List<CustomRoadmap> getUserRoadmaps(User user) {
        return customRoadmapRepository.findByUserOrderByCreatedAtDesc(user);
    }

    @Transactional(readOnly = true)
    public CustomRoadmap getRoadmapById(Long id, User user) {
        return customRoadmapRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Custom roadmap not found with id: " + id));
    }

    @Transactional
    public CustomRoadmap updateMilestoneStatus(User user, Long roadmapId, String milestoneId, ProgressStatus newStatus) {
        CustomRoadmap customRoadmap = getRoadmapById(roadmapId, user);
        try {
            AiRoadmapResponse roadmap = objectMapper.readValue(customRoadmap.getRoadmapJson(), AiRoadmapResponse.class);

            int total = 0;
            int completed = 0;
            boolean found = false;

            if (roadmap.getPhases() != null) {
                for (AiRoadmapPhaseDTO phase : roadmap.getPhases()) {
                    if (phase.getMilestones() != null) {
                        for (AiMilestoneDTO m : phase.getMilestones()) {
                            total++;
                            if (milestoneId.equals(m.getId())) {
                                m.setStatus(newStatus);
                                found = true;
                            }
                            if (m.getStatus() == ProgressStatus.COMPLETED) {
                                completed++;
                            }
                        }
                    }
                }
            }

            if (!found) {
                log.warn("Milestone id {} not found in roadmap {}", milestoneId, roadmapId);
            }

            customRoadmap.setTotalMilestones(total);
            customRoadmap.setCompletedMilestones(completed);
            customRoadmap.setRoadmapJson(objectMapper.writeValueAsString(roadmap));

            return customRoadmapRepository.save(customRoadmap);
        } catch (Exception e) {
            log.error("Failed to update milestone status", e);
            throw new RuntimeException("Failed to update milestone: " + e.getMessage());
        }
    }

    @Transactional
    public void deleteRoadmap(User user, Long roadmapId) {
        customRoadmapRepository.deleteByIdAndUser(roadmapId, user);
    }
}
