package com.skillforge.dto;

public class CandidateRosterDTO {

    private Integer userId;
    private String fullName;
    private String email;
    private String department;
    private String targetRoleName;
    private int score;
    private int acquiredCount;
    private int totalRequiredCount;
    private int missingCoreCount;
    private boolean jobReady;

    public CandidateRosterDTO() {
    }

    public CandidateRosterDTO(Integer userId, String fullName, String email, String department,
                              String targetRoleName, int score, int acquiredCount,
                              int totalRequiredCount, int missingCoreCount, boolean jobReady) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.department = department;
        this.targetRoleName = targetRoleName;
        this.score = score;
        this.acquiredCount = acquiredCount;
        this.totalRequiredCount = totalRequiredCount;
        this.missingCoreCount = missingCoreCount;
        this.jobReady = jobReady;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getTargetRoleName() {
        return targetRoleName;
    }

    public void setTargetRoleName(String targetRoleName) {
        this.targetRoleName = targetRoleName;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getAcquiredCount() {
        return acquiredCount;
    }

    public void setAcquiredCount(int acquiredCount) {
        this.acquiredCount = acquiredCount;
    }

    public int getTotalRequiredCount() {
        return totalRequiredCount;
    }

    public void setTotalRequiredCount(int totalRequiredCount) {
        this.totalRequiredCount = totalRequiredCount;
    }

    public int getMissingCoreCount() {
        return missingCoreCount;
    }

    public void setMissingCoreCount(int missingCoreCount) {
        this.missingCoreCount = missingCoreCount;
    }

    public boolean isJobReady() {
        return jobReady;
    }

    public void setJobReady(boolean jobReady) {
        this.jobReady = jobReady;
    }
}
