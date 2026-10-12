package com.skillforge.dto;

import com.skillforge.model.SkillCategory;
import com.skillforge.model.SkillPriority;
import com.skillforge.model.SkillProficiency;

public class ParsedSkillDTO {

    private Integer skillId;
    private String skillName;
    private SkillCategory category;
    private SkillProficiency proficiency;
    private double confidence;
    private String snippet;
    private boolean alreadyInProfile;
    private boolean selected = true;

    // Advanced evidence & verification fields
    private String evidenceSnippet;
    private String sectionName;
    private String evidenceType; // "PRACTICAL_PROJECT", "WORK_EXPERIENCE", "CERTIFICATION", "SKILLS_LISTING", "GENERAL"
    private String reasoning;
    private String confidenceLevel = "HIGH"; // "HIGH", "MEDIUM", "LOW"
    private boolean needsReview;
    private String profileStatus = "NEW_COMPETENCY"; // "NEW_COMPETENCY", "EXISTING_CONFIRMED", "UPGRADE_SUGGESTED", "DISCREPANCY_FLAGGED"
    private SkillProficiency currentProficiency;
    private boolean isTargetRoleRequired;
    private SkillPriority targetRolePriority;
    private Integer targetRoleWeight;

    public ParsedSkillDTO() {
    }

    public ParsedSkillDTO(Integer skillId, String skillName, SkillCategory category,
                          SkillProficiency proficiency, double confidence, String snippet,
                          boolean alreadyInProfile) {
        this.skillId = skillId;
        this.skillName = skillName;
        this.category = category;
        this.proficiency = proficiency;
        this.confidence = confidence;
        this.snippet = snippet;
        this.evidenceSnippet = snippet;
        this.alreadyInProfile = alreadyInProfile;
        this.selected = !alreadyInProfile;
    }

    public Integer getSkillId() {
        return skillId;
    }

    public void setSkillId(Integer skillId) {
        this.skillId = skillId;
    }

    public String getSkillName() {
        return skillName;
    }

    public void setSkillName(String skillName) {
        this.skillName = skillName;
    }

    public SkillCategory getCategory() {
        return category;
    }

    public void setCategory(SkillCategory category) {
        this.category = category;
    }

    public SkillProficiency getProficiency() {
        return proficiency;
    }

    public void setProficiency(SkillProficiency proficiency) {
        this.proficiency = proficiency;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
        if (confidence >= 0.85) {
            this.confidenceLevel = "HIGH";
        } else if (confidence >= 0.70) {
            this.confidenceLevel = "MEDIUM";
        } else {
            this.confidenceLevel = "LOW";
        }
    }

    public String getSnippet() {
        return snippet;
    }

    public void setSnippet(String snippet) {
        this.snippet = snippet;
        if (this.evidenceSnippet == null) {
            this.evidenceSnippet = snippet;
        }
    }

    public boolean isAlreadyInProfile() {
        return alreadyInProfile;
    }

    public void setAlreadyInProfile(boolean alreadyInProfile) {
        this.alreadyInProfile = alreadyInProfile;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public String getEvidenceSnippet() {
        return evidenceSnippet;
    }

    public void setEvidenceSnippet(String evidenceSnippet) {
        this.evidenceSnippet = evidenceSnippet;
    }

    public String getSectionName() {
        return sectionName;
    }

    public void setSectionName(String sectionName) {
        this.sectionName = sectionName;
    }

    public String getEvidenceType() {
        return evidenceType;
    }

    public void setEvidenceType(String evidenceType) {
        this.evidenceType = evidenceType;
    }

    public String getReasoning() {
        return reasoning;
    }

    public void setReasoning(String reasoning) {
        this.reasoning = reasoning;
    }

    public String getConfidenceLevel() {
        return confidenceLevel;
    }

    public void setConfidenceLevel(String confidenceLevel) {
        this.confidenceLevel = confidenceLevel;
    }

    public boolean isNeedsReview() {
        return needsReview;
    }

    public void setNeedsReview(boolean needsReview) {
        this.needsReview = needsReview;
    }

    public String getProfileStatus() {
        return profileStatus;
    }

    public void setProfileStatus(String profileStatus) {
        this.profileStatus = profileStatus;
    }

    public SkillProficiency getCurrentProficiency() {
        return currentProficiency;
    }

    public void setCurrentProficiency(SkillProficiency currentProficiency) {
        this.currentProficiency = currentProficiency;
    }

    public boolean isTargetRoleRequired() {
        return isTargetRoleRequired;
    }

    public void setTargetRoleRequired(boolean targetRoleRequired) {
        isTargetRoleRequired = targetRoleRequired;
    }

    public SkillPriority getTargetRolePriority() {
        return targetRolePriority;
    }

    public void setTargetRolePriority(SkillPriority targetRolePriority) {
        this.targetRolePriority = targetRolePriority;
    }

    public Integer getTargetRoleWeight() {
        return targetRoleWeight;
    }

    public void setTargetRoleWeight(Integer targetRoleWeight) {
        this.targetRoleWeight = targetRoleWeight;
    }
}

