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

    public ResumeParseResponse() {
    }

    public ResumeParseResponse(String filename, String fileType, int extractedTextLength,
                               String summarySnippet, List<ParsedSkillDTO> detectedSkills) {
        this.filename = filename;
        this.fileType = fileType;
        this.extractedTextLength = extractedTextLength;
        this.summarySnippet = summarySnippet;
        this.detectedSkills = detectedSkills != null ? detectedSkills : new ArrayList<>();
        this.newSkillsCount = (int) this.detectedSkills.stream().filter(s -> !s.isAlreadyInProfile()).count();
        this.existingSkillsCount = (int) this.detectedSkills.stream().filter(ParsedSkillDTO::isAlreadyInProfile).count();
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
        if (detectedSkills != null) {
            this.newSkillsCount = (int) detectedSkills.stream().filter(s -> !s.isAlreadyInProfile()).count();
            this.existingSkillsCount = (int) detectedSkills.stream().filter(ParsedSkillDTO::isAlreadyInProfile).count();
        }
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
}
