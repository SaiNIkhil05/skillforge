package com.skillforge.dto;

import java.util.ArrayList;
import java.util.List;

public class ResumeParseResponse {

    private String filename;
    private String fileType;
    private int extractedTextLength;
    private String summarySnippet;
    private List<ParsedSkillDTO> detectedSkills = new ArrayList<>();
    private int newSkillsCount;
    private int existingSkillsCount;
    private int upgradesCount;
    private int needsReviewCount;
    private String targetRoleName;
    private int targetRoleCoreCoveredCount;
    private int targetRoleCoreTotalCount;
    private List<String> missingCoreSkills = new ArrayList<>();
    private List<String> detectedSections = new ArrayList<>();
    private List<String> detectedCertifications = new ArrayList<>();
    private double estimatedReadinessBoost;

    public ResumeParseResponse() {
    }

    public ResumeParseResponse(String filename, String fileType, int extractedTextLength,
                               String summarySnippet, List<ParsedSkillDTO> detectedSkills) {
        this.filename = filename;
        this.fileType = fileType;
        this.extractedTextLength = extractedTextLength;
        this.summarySnippet = summarySnippet;
        this.detectedSkills = detectedSkills != null ? detectedSkills : new ArrayList<>();
        recalculateCounts();
    }

    public void recalculateCounts() {
        if (this.detectedSkills != null) {
            this.newSkillsCount = (int) this.detectedSkills.stream().filter(s -> !s.isAlreadyInProfile()).count();
            this.existingSkillsCount = (int) this.detectedSkills.stream().filter(ParsedSkillDTO::isAlreadyInProfile).count();
            this.upgradesCount = (int) this.detectedSkills.stream().filter(s -> "UPGRADE_SUGGESTED".equals(s.getProfileStatus())).count();
            this.needsReviewCount = (int) this.detectedSkills.stream().filter(ParsedSkillDTO::isNeedsReview).count();
        }
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public int getExtractedTextLength() {
        return extractedTextLength;
    }

    public void setExtractedTextLength(int extractedTextLength) {
        this.extractedTextLength = extractedTextLength;
    }

    public String getSummarySnippet() {
        return summarySnippet;
    }

    public void setSummarySnippet(String summarySnippet) {
        this.summarySnippet = summarySnippet;
    }

    public List<ParsedSkillDTO> getDetectedSkills() {
        return detectedSkills;
    }

    public void setDetectedSkills(List<ParsedSkillDTO> detectedSkills) {
        this.detectedSkills = detectedSkills;
        recalculateCounts();
    }

    public int getNewSkillsCount() {
        return newSkillsCount;
    }

    public void setNewSkillsCount(int newSkillsCount) {
        this.newSkillsCount = newSkillsCount;
    }

    public int getExistingSkillsCount() {
        return existingSkillsCount;
    }

    public void setExistingSkillsCount(int existingSkillsCount) {
        this.existingSkillsCount = existingSkillsCount;
    }

    public int getUpgradesCount() {
        return upgradesCount;
    }

    public void setUpgradesCount(int upgradesCount) {
        this.upgradesCount = upgradesCount;
    }

    public int getNeedsReviewCount() {
        return needsReviewCount;
    }

    public void setNeedsReviewCount(int needsReviewCount) {
        this.needsReviewCount = needsReviewCount;
    }

    public String getTargetRoleName() {
        return targetRoleName;
    }

    public void setTargetRoleName(String targetRoleName) {
        this.targetRoleName = targetRoleName;
    }

    public int getTargetRoleCoreCoveredCount() {
        return targetRoleCoreCoveredCount;
    }

    public void setTargetRoleCoreCoveredCount(int targetRoleCoreCoveredCount) {
        this.targetRoleCoreCoveredCount = targetRoleCoreCoveredCount;
    }

    public int getTargetRoleCoreTotalCount() {
        return targetRoleCoreTotalCount;
    }

    public void setTargetRoleCoreTotalCount(int targetRoleCoreTotalCount) {
        this.targetRoleCoreTotalCount = targetRoleCoreTotalCount;
    }

    public List<String> getMissingCoreSkills() {
        return missingCoreSkills;
    }

    public void setMissingCoreSkills(List<String> missingCoreSkills) {
        this.missingCoreSkills = missingCoreSkills;
    }

    public List<String> getDetectedSections() {
        return detectedSections;
    }

    public void setDetectedSections(List<String> detectedSections) {
        this.detectedSections = detectedSections;
    }

    public List<String> getDetectedCertifications() {
        return detectedCertifications;
    }

    public void setDetectedCertifications(List<String> detectedCertifications) {
        this.detectedCertifications = detectedCertifications;
    }

    public double getEstimatedReadinessBoost() {
        return estimatedReadinessBoost;
    }

    public void setEstimatedReadinessBoost(double estimatedReadinessBoost) {
        this.estimatedReadinessBoost = estimatedReadinessBoost;
    }
}
