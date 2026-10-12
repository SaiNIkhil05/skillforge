package com.skillforge.service;

import com.skillforge.exception.ResourceNotFoundException;
import com.skillforge.model.Skill;
import com.skillforge.model.SkillCategory;
import com.skillforge.repository.SkillRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SkillService {

    private final SkillRepository skillRepository;

    public SkillService(SkillRepository skillRepository) {
        this.skillRepository = skillRepository;
    }

    public List<Skill> getAllSkills() {
        return skillRepository.findAllByOrderBySkillNameAsc();
    }

    public List<Skill> getSkillsByCategory(SkillCategory category) {
        return skillRepository.findByCategoryOrderBySkillNameAsc(category);
    }

    public Skill getSkillById(Integer skillId) {
        return skillRepository.findById(skillId)
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found with ID: " + skillId));
    }

    @Transactional
    public Skill createSkill(String skillName, SkillCategory category) {
        if (skillRepository.existsBySkillNameIgnoreCase(skillName.trim())) {
            throw new IllegalArgumentException("A skill with name '" + skillName.trim() + "' already exists");
        }
        Skill skill = new Skill();
        skill.setSkillName(skillName.trim());
        skill.setCategory(category != null ? category : SkillCategory.LANGUAGE);
        return skillRepository.save(skill);
    }

    public long getTotalSkillsCount() {
        return skillRepository.count();
    }
}
