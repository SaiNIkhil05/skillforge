package com.skillforge.dto;

import jakarta.validation.constraints.NotBlank;

public class GenerateRoadmapRequest {

    @NotBlank(message = "Desired tech stack cannot be blank")
    private String desiredTechStack;

    private String targetRole;
    private String targetLevel = "Junior to Mid-Level";
    private Integer timeframeWeeks = 12;
    private String learningFocus = "Project-Driven";
    private String customApiKey;

    public GenerateRoadmapRequest() {
    }

    public GenerateRoadmapRequest(String desiredTechStack) {
        this.desiredTechStack = desiredTechStack;
    }

    public String getDesiredTechStack() {
        return desiredTechStack;
    }

    public void setDesiredTechStack(String desiredTechStack) {
        this.desiredTechStack = desiredTechStack;
    }

    public String getTargetRole() {
        return targetRole;
    }

    public void setTargetRole(String targetRole) {
        this.targetRole = targetRole;
    }

    public String getTargetLevel() {
        return targetLevel;
    }

    public void setTargetLevel(String targetLevel) {
        this.targetLevel = targetLevel;
    }

    public Integer getTimeframeWeeks() {
        return timeframeWeeks;
    }

    public void setTimeframeWeeks(Integer timeframeWeeks) {
        this.timeframeWeeks = timeframeWeeks;
    }

    public String getLearningFocus() {
        return learningFocus;
    }

    public void setLearningFocus(String learningFocus) {
        this.learningFocus = learningFocus;
    }

    public String getCustomApiKey() {
        return customApiKey;
    }

    public void setCustomApiKey(String customApiKey) {
        this.customApiKey = customApiKey;
    }
}
