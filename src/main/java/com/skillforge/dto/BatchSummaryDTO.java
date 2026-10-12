package com.skillforge.dto;

import java.util.HashMap;
import java.util.Map;

public class BatchSummaryDTO {

    private double averageReadinessScore;
    private long totalStudents;
    private long jobReadyCount;
    private double jobReadyPercentage;
    private long criticalGapCount;
    private Map<String, Long> gapsByCategory = new HashMap<>();
    private Map<String, Long> studentDistributionByRole = new HashMap<>();
    private Map<String, Double> averageScoreByRole = new HashMap<>();

    public BatchSummaryDTO() {
    }

    public double getAverageReadinessScore() {
        return averageReadinessScore;
    }

    public void setAverageReadinessScore(double averageReadinessScore) {
        this.averageReadinessScore = averageReadinessScore;
    }

    public long getTotalStudents() {
        return totalStudents;
    }

    public void setTotalStudents(long totalStudents) {
        this.totalStudents = totalStudents;
    }

    public long getJobReadyCount() {
        return jobReadyCount;
    }

    public void setJobReadyCount(long jobReadyCount) {
        this.jobReadyCount = jobReadyCount;
    }

    public double getJobReadyPercentage() {
        return jobReadyPercentage;
    }

    public void setJobReadyPercentage(double jobReadyPercentage) {
        this.jobReadyPercentage = jobReadyPercentage;
    }

    public long getCriticalGapCount() {
        return criticalGapCount;
    }

    public void setCriticalGapCount(long criticalGapCount) {
        this.criticalGapCount = criticalGapCount;
    }

    public Map<String, Long> getGapsByCategory() {
        return gapsByCategory;
    }

    public void setGapsByCategory(Map<String, Long> gapsByCategory) {
        this.gapsByCategory = gapsByCategory;
    }

    public Map<String, Long> getStudentDistributionByRole() {
        return studentDistributionByRole;
    }

    public void setStudentDistributionByRole(Map<String, Long> studentDistributionByRole) {
        this.studentDistributionByRole = studentDistributionByRole;
    }

    public Map<String, Double> getAverageScoreByRole() {
        return averageScoreByRole;
    }

    public void setAverageScoreByRole(Map<String, Double> averageScoreByRole) {
        this.averageScoreByRole = averageScoreByRole;
    }
}
