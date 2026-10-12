package com.skillforge.dto;

import java.util.ArrayList;
import java.util.List;

public class AiRoadmapPhaseDTO {

    private int phaseNumber;
    private String phaseTitle;
    private String phaseGoal;
    private int estimatedWeeks;
    private List<AiMilestoneDTO> milestones = new ArrayList<>();

    public AiRoadmapPhaseDTO() {
    }

    public AiRoadmapPhaseDTO(int phaseNumber, String phaseTitle, String phaseGoal, int estimatedWeeks, List<AiMilestoneDTO> milestones) {
        this.phaseNumber = phaseNumber;
        this.phaseTitle = phaseTitle;
        this.phaseGoal = phaseGoal;
        this.estimatedWeeks = estimatedWeeks;
        this.milestones = milestones != null ? milestones : new ArrayList<>();
    }

    public int getPhaseNumber() {
        return phaseNumber;
    }

    public void setPhaseNumber(int phaseNumber) {
        this.phaseNumber = phaseNumber;
    }

    public String getPhaseTitle() {
        return phaseTitle;
    }

    public void setPhaseTitle(String phaseTitle) {
        this.phaseTitle = phaseTitle;
    }

    public String getPhaseGoal() {
        return phaseGoal;
    }

    public void setPhaseGoal(String phaseGoal) {
        this.phaseGoal = phaseGoal;
    }

    public int getEstimatedWeeks() {
        return estimatedWeeks;
    }

    public void setEstimatedWeeks(int estimatedWeeks) {
        this.estimatedWeeks = estimatedWeeks;
    }

    public List<AiMilestoneDTO> getMilestones() {
        return milestones;
    }

    public void setMilestones(List<AiMilestoneDTO> milestones) {
        this.milestones = milestones;
    }
}
