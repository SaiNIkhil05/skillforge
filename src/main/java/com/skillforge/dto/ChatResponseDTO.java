package com.skillforge.dto;

public class ChatResponseDTO {
    private String reply;
    private AiRoadmapResponse roadmap;
    private String modelUsed = "Google Gemini / Live AI";

    public ChatResponseDTO() {
    }

    public ChatResponseDTO(String reply, AiRoadmapResponse roadmap, String modelUsed) {
        this.reply = reply;
        this.roadmap = roadmap;
        this.modelUsed = modelUsed;
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }

    public AiRoadmapResponse getRoadmap() {
        return roadmap;
    }

    public void setRoadmap(AiRoadmapResponse roadmap) {
        this.roadmap = roadmap;
    }

    public String getModelUsed() {
        return modelUsed;
    }

    public void setModelUsed(String modelUsed) {
        this.modelUsed = modelUsed;
    }
}
