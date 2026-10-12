package com.skillforge.service;

import com.skillforge.model.SkillProficiency;

public final class ProficiencyMultipliers {

    public static final double BEGINNER_MULTIPLIER = 0.5;
    public static final double INTERMEDIATE_MULTIPLIER = 0.8;
    public static final double ADVANCED_MULTIPLIER = 1.0;

    private ProficiencyMultipliers() {
    }

    public static double getMultiplier(SkillProficiency proficiency) {
        if (proficiency == null) {
            return BEGINNER_MULTIPLIER;
        }
        return switch (proficiency) {
            case BEGINNER -> BEGINNER_MULTIPLIER;
            case INTERMEDIATE -> INTERMEDIATE_MULTIPLIER;
            case ADVANCED -> ADVANCED_MULTIPLIER;
        };
    }
}
