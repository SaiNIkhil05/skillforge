package com.skillforge.dto;

import jakarta.validation.constraints.NotBlank;

public class ProjectRequest {

    @NotBlank(message = "Project title is required")
    private String title;

    @NotBlank(message = "Technologies used must be specified")
    private String techUsed;

    private String projectLink;

    public ProjectRequest() {
    }

    public ProjectRequest(String title, String techUsed, String projectLink) {
        this.title = title;
        this.techUsed = techUsed;
        this.projectLink = projectLink;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTechUsed() {
        return techUsed;
    }

    public void setTechUsed(String techUsed) {
        this.techUsed = techUsed;
    }

    public String getProjectLink() {
        return projectLink;
    }

    public void setProjectLink(String projectLink) {
        this.projectLink = projectLink;
    }
}
