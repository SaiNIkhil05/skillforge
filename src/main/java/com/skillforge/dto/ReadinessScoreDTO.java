package com.skillforge.dto;

import com.skillforge.model.RoleRequiredSkill;

import java.util.ArrayList;
import java.util.List;

public class ReadinessScoreDTO {
    private int score;
    private int totalWeight;
    private double earnedWeight;
    private Integer roleId;
    private String roleName;
    private int totalRequiredCount;
    private int acquiredCount;
    private int missingCoreCount;
    private int missingGoodToHaveCount;
    private String statusMessage;
    private List<RoleRequiredSkill> missingCoreSkills = new ArrayList<>();
    private List<RoleRequiredSkill> missingGoodToHaveSkills = new ArrayList<>();
    private List<MatchedSkillDTO> matchedSkills = new ArrayList<>();
    private int completedRoadmapCount;
    private int totalRoadmapCount;

    public ReadinessScoreDTO() {
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getTotalWeight() {
        return totalWeight;
    }

    public void setTotalWeight(int totalWeight) {
        this.totalWeight = totalWeight;
    }

    public double getEarnedWeight() {
        return earnedWeight;
    }

    public void setEarnedWeight(double earnedWeight) {
        this.earnedWeight = earnedWeight;
    }

    public Integer getRoleId() {
        return roleId;
    }

    public void setRoleId(Integer roleId) {
        this.roleId = roleId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public int getTotalRequiredCount() {
        return totalRequiredCount;
    }

    public void setTotalRequiredCount(int totalRequiredCount) {
        this.totalRequiredCount = totalRequiredCount;
    }

    public int getAcquiredCount() {
        return acquiredCount;
    }

    public void setAcquiredCount(int acquiredCount) {
        this.acquiredCount = acquiredCount;
    }

    public int getMissingCoreCount() {
        return missingCoreCount;
    }

    public void setMissingCoreCount(int missingCoreCount) {
        this.missingCoreCount = missingCoreCount;
    }

    public int getMissingGoodToHaveCount() {
        return missingGoodToHaveCount;
    }

    public void setMissingGoodToHaveCount(int missingGoodToHaveCount) {
        this.missingGoodToHaveCount = missingGoodToHaveCount;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public List<RoleRequiredSkill> getMissingCoreSkills() {
        return missingCoreSkills;
    }

    public void setMissingCoreSkills(List<RoleRequiredSkill> missingCoreSkills) {
        this.missingCoreSkills = missingCoreSkills;
    }

    public List<RoleRequiredSkill> getMissingGoodToHaveSkills() {
        return missingGoodToHaveSkills;
    }

    public void setMissingGoodToHaveSkills(List<RoleRequiredSkill> missingGoodToHaveSkills) {
        this.missingGoodToHaveSkills = missingGoodToHaveSkills;
    }

    public List<MatchedSkillDTO> getMatchedSkills() {
        return matchedSkills;
    }

    public void setMatchedSkills(List<MatchedSkillDTO> matchedSkills) {
        this.matchedSkills = matchedSkills;
    }

    public int getCompletedRoadmapCount() {
        return completedRoadmapCount;
    }

    public void setCompletedRoadmapCount(int completedRoadmapCount) {
        this.completedRoadmapCount = completedRoadmapCount;
    }

    public int getTotalRoadmapCount() {
        return totalRoadmapCount;
    }

    public void setTotalRoadmapCount(int totalRoadmapCount) {
        this.totalRoadmapCount = totalRoadmapCount;
    }
}
