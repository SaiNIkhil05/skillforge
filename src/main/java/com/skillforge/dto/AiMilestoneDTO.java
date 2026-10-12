package com.skillforge.dto;

import com.skillforge.model.ProgressStatus;
import java.util.ArrayList;
import java.util.List;

public class AiMilestoneDTO {

    private String id;
    private String title;
    private String summary;
    private List<String> competencies = new ArrayList<>();
    private List<String> keyTopics = new ArrayList<>();
    private String projectDeliverable;
    private List<String> recommendedResources = new ArrayList<>();
    private ProgressStatus status = ProgressStatus.NOT_STARTED;
    private int estimatedHours = 10;

    public AiMilestoneDTO() {
    }

    public AiMilestoneDTO(String id, String title, String summary, List<String> competencies,
                          List<String> keyTopics, String projectDeliverable,
                          List<String> recommendedResources, int estimatedHours) {
        this.id = id;
        this.title = title;
        this.summary = summary;
        this.competencies = competencies != null ? competencies : new ArrayList<>();
        this.keyTopics = keyTopics != null ? keyTopics : new ArrayList<>();
        this.projectDeliverable = projectDeliverable;
        this.recommendedResources = recommendedResources != null ? recommendedResources : new ArrayList<>();
        this.estimatedHours = estimatedHours;
        this.status = ProgressStatus.NOT_STARTED;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public List<String> getCompetencies() {
        return competencies;
    }

    public void setCompetencies(List<String> competencies) {
        this.competencies = competencies;
    }

    public List<String> getKeyTopics() {
        return keyTopics;
    }

    public void setKeyTopics(List<String> keyTopics) {
        this.keyTopics = keyTopics;
    }

    public String getProjectDeliverable() {
        return projectDeliverable;
    }

    public void setProjectDeliverable(String projectDeliverable) {
        this.projectDeliverable = projectDeliverable;
    }

    public List<String> getRecommendedResources() {
        return recommendedResources;
    }

    public void setRecommendedResources(List<String> recommendedResources) {
        this.recommendedResources = recommendedResources;
    }

    public ProgressStatus getStatus() {
        return status;
    }

    public void setStatus(ProgressStatus status) {
        this.status = status;
    }

    public int getEstimatedHours() {
        return estimatedHours;
    }

    public void setEstimatedHours(int estimatedHours) {
        this.estimatedHours = estimatedHours;
    }
}
