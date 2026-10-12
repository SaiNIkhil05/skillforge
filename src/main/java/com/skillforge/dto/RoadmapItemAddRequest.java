package com.skillforge.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class RoadmapItemAddRequest {

    private Integer skillId;

    @NotBlank(message = "Title is required")
    private String title;

    @NotNull(message = "Order index is required")
    @Min(value = 1, message = "Order index must be at least 1")
    private Integer orderIndex = 1;

    public RoadmapItemAddRequest() {
    }

    public RoadmapItemAddRequest(Integer skillId, String title, Integer orderIndex) {
        this.skillId = skillId;
        this.title = title;
        this.orderIndex = orderIndex;
    }

    public Integer getSkillId() {
        return skillId;
    }

    public void setSkillId(Integer skillId) {
        this.skillId = skillId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(Integer orderIndex) {
        this.orderIndex = orderIndex;
    }
}
