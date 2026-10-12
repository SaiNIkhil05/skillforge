package com.skillforge.dto;

import java.util.ArrayList;
import java.util.List;

public class AiRoadmapResponse {

    private Long id;
    private String techStack;
    private String targetRole;
    private String experienceLevel;
    private int timeframeWeeks;
    private String learningFocus;
    private String executiveSummary;
    private String architectureOverview;
    private List<String> prerequisites = new ArrayList<>();
    private List<AiRoadmapPhaseDTO> phases = new ArrayList<>();
    private List<String> recommendedProjects = new ArrayList<>();
    private String aiThinkingNotes;
    private String generatedBy = "Google Gemini 1.5 Flash";
    private boolean saved = false;

    public AiRoadmapResponse() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTechStack() {
        return techStack;
    }

    public void setTechStack(String techStack) {
        this.techStack = techStack;
    }

    public String getTargetRole() {
        return targetRole;
    }

    public void setTargetRole(String targetRole) {
        this.targetRole = targetRole;
    }

    public String getExperienceLevel() {
        return experienceLevel;
    }

    public void setExperienceLevel(String experienceLevel) {
        this.experienceLevel = experienceLevel;
    }

    public int getTimeframeWeeks() {
        return timeframeWeeks;
    }

    public void setTimeframeWeeks(int timeframeWeeks) {
        this.timeframeWeeks = timeframeWeeks;
    }

    public String getLearningFocus() {
        return learningFocus;
    }

    public void setLearningFocus(String learningFocus) {
        this.learningFocus = learningFocus;
    }

    public String getExecutiveSummary() {
        return executiveSummary;
    }

    public void setExecutiveSummary(String executiveSummary) {
        this.executiveSummary = executiveSummary;
    }

    public String getArchitectureOverview() {
        return architectureOverview;
    }

    public void setArchitectureOverview(String architectureOverview) {
        this.architectureOverview = architectureOverview;
    }

    public List<String> getPrerequisites() {
        return prerequisites;
    }

    public void setPrerequisites(List<String> prerequisites) {
        this.prerequisites = prerequisites != null ? prerequisites : new ArrayList<>();
    }

    public List<AiRoadmapPhaseDTO> getPhases() {
        return phases;
    }

    public void setPhases(List<AiRoadmapPhaseDTO> phases) {
        this.phases = phases != null ? phases : new ArrayList<>();
    }

    public List<String> getRecommendedProjects() {
        return recommendedProjects;
    }

    public void setRecommendedProjects(List<String> recommendedProjects) {
        this.recommendedProjects = recommendedProjects != null ? recommendedProjects : new ArrayList<>();
    }

    public String getAiThinkingNotes() {
        return aiThinkingNotes;
    }

    public void setAiThinkingNotes(String aiThinkingNotes) {
        this.aiThinkingNotes = aiThinkingNotes;
    }

    public String getGeneratedBy() {
        return generatedBy;
    }

    public void setGeneratedBy(String generatedBy) {
        this.generatedBy = generatedBy;
    }

    public boolean isSaved() {
        return saved;
    }

    public void setSaved(boolean saved) {
        this.saved = saved;
    }
}
