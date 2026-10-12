package com.skillforge.service;

import com.skillforge.dto.BatchSummaryDTO;
import com.skillforge.dto.CandidateRosterDTO;
import com.skillforge.dto.HeatmapCellDTO;
import com.skillforge.dto.ReadinessScoreDTO;
import com.skillforge.model.CareerRole;
import com.skillforge.model.RoleRequiredSkill;
import com.skillforge.model.User;
import com.skillforge.model.UserRole;
import com.skillforge.repository.CareerRoleRepository;
import com.skillforge.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdminAnalyticsService {

    private final UserRepository userRepository;
    private final CareerRoleRepository careerRoleRepository;
    private final ReadinessScoreService readinessScoreService;

    public AdminAnalyticsService(
            UserRepository userRepository,
            CareerRoleRepository careerRoleRepository,
            ReadinessScoreService readinessScoreService) {
        this.userRepository = userRepository;
        this.careerRoleRepository = careerRoleRepository;
        this.readinessScoreService = readinessScoreService;
    }

    public BatchSummaryDTO getBatchSummary() {
        List<User> students = userRepository.findAll().stream()
                .filter(u -> u.getRole() == UserRole.STUDENT)
                .collect(Collectors.toList());

        BatchSummaryDTO summary = new BatchSummaryDTO();
        summary.setTotalStudents(students.size());

        if (students.isEmpty()) {
            return summary;
        }

        double totalScoreSum = 0;
        long jobReadyCount = 0;
        long totalCriticalGaps = 0;
        Map<String, Long> gapsByCategory = new HashMap<>();
        Map<String, Long> roleDistribution = new HashMap<>();
        Map<String, List<Integer>> scoresByRole = new HashMap<>();

        for (User student : students) {
            ReadinessScoreDTO readiness = readinessScoreService.calculateReadiness(student);
            int score = readiness.getScore();
            totalScoreSum += score;

            if (score >= 70) {
                jobReadyCount++;
            }

            totalCriticalGaps += readiness.getMissingCoreCount();

            // Aggregate missing skills by category
            if (readiness.getMissingCoreSkills() != null) {
                for (RoleRequiredSkill rrs : readiness.getMissingCoreSkills()) {
                    String catName = rrs.getSkill().getCategory().name();
                    gapsByCategory.put(catName, gapsByCategory.getOrDefault(catName, 0L) + 1);
                }
            }
            if (readiness.getMissingGoodToHaveSkills() != null) {
                for (RoleRequiredSkill rrs : readiness.getMissingGoodToHaveSkills()) {
                    String catName = rrs.getSkill().getCategory().name();
                    gapsByCategory.put(catName, gapsByCategory.getOrDefault(catName, 0L) + 1);
                }
            }

            // Role distribution
            String roleName = student.getTargetRole() != null ? student.getTargetRole().getRoleName() : "Undecided";
            roleDistribution.put(roleName, roleDistribution.getOrDefault(roleName, 0L) + 1);

            scoresByRole.computeIfAbsent(roleName, k -> new ArrayList<>()).add(score);
        }

        summary.setAverageReadinessScore(Math.round((totalScoreSum / students.size()) * 10.0) / 10.0);
        summary.setJobReadyCount(jobReadyCount);
        summary.setJobReadyPercentage(Math.round(((double) jobReadyCount / students.size()) * 1000.0) / 10.0);
        summary.setCriticalGapCount(totalCriticalGaps);
        summary.setGapsByCategory(gapsByCategory);
        summary.setStudentDistributionByRole(roleDistribution);

        Map<String, Double> averageScoreByRole = new HashMap<>();
        scoresByRole.forEach((role, scores) -> {
            double avg = scores.stream().mapToInt(Integer::intValue).average().orElse(0.0);
            averageScoreByRole.put(role, Math.round(avg * 10.0) / 10.0);
        });
        summary.setAverageScoreByRole(averageScoreByRole);

        return summary;
    }

    public List<HeatmapCellDTO> getHeatmapData() {
        List<User> students = userRepository.findAll().stream()
                .filter(u -> u.getRole() == UserRole.STUDENT)
                .collect(Collectors.toList());

        List<CareerRole> allRoles = careerRoleRepository.findAll();

        // Extract known departments (defaulting to "Computer Science" if unassigned)
        Set<String> departments = new LinkedHashSet<>();
        departments.add("Computer Science");
        departments.add("Information Technology");
        departments.add("Electronics & Comm.");
        departments.add("Data Science");

        for (User s : students) {
            String dept = resolveDepartment(s);
            departments.add(dept);
        }

        // Precompute readiness scores per student
        Map<Integer, Integer> studentScores = new HashMap<>();
        for (User s : students) {
            studentScores.put(s.getUserId(), readinessScoreService.calculateReadiness(s).getScore());
        }

        List<HeatmapCellDTO> matrix = new ArrayList<>();

        for (String dept : departments) {
            for (CareerRole role : allRoles) {
                List<User> group = students.stream()
                        .filter(s -> dept.equalsIgnoreCase(resolveDepartment(s)))
                        .filter(s -> s.getTargetRole() != null && s.getTargetRole().getRoleId().equals(role.getRoleId()))
                        .collect(Collectors.toList());

                int count = group.size();
                int avgScore = 0;
                int jobReadyCount = 0;

                if (count > 0) {
                    double sum = 0;
                    for (User s : group) {
                        int score = studentScores.getOrDefault(s.getUserId(), 0);
                        sum += score;
                        if (score >= 70) jobReadyCount++;
                    }
                    avgScore = (int) Math.round(sum / count);
                }

                double rate = count > 0 ? Math.round(((double) jobReadyCount / count) * 1000.0) / 10.0 : 0.0;

                matrix.add(new HeatmapCellDTO(
                        dept,
                        role.getRoleId(),
                        role.getRoleName(),
                        count,
                        avgScore,
                        jobReadyCount,
                        rate
                ));
            }
        }

        return matrix;
    }

    public List<CandidateRosterDTO> getCandidateRoster(Integer minScore, Integer roleId) {
        int min = minScore != null ? minScore : 0;

        return userRepository.findAll().stream()
                .filter(u -> u.getRole() == UserRole.STUDENT)
                .filter(u -> roleId == null || (u.getTargetRole() != null && u.getTargetRole().getRoleId().equals(roleId)))
                .map(student -> {
                    ReadinessScoreDTO r = readinessScoreService.calculateReadiness(student);
                    return new CandidateRosterDTO(
                            student.getUserId(),
                            student.getFullName(),
                            student.getEmail(),
                            resolveDepartment(student),
                            r.getRoleName(),
                            r.getScore(),
                            r.getAcquiredCount(),
                            r.getTotalRequiredCount(),
                            r.getMissingCoreCount(),
                            r.getScore() >= 70
                    );
                })
                .filter(c -> c.getScore() >= min)
                .sorted((a, b) -> Integer.compare(b.getScore(), a.getScore()))
                .collect(Collectors.toList());
    }

    public String generateCandidatesCsv(Integer minScore, Integer roleId) {
        List<CandidateRosterDTO> roster = getCandidateRoster(minScore, roleId);
        StringBuilder sb = new StringBuilder();

        // CSV Header
        sb.append("Student ID,Full Name,Email,Department,Target Role,Readiness Score (%),Acquired Skills,Missing Core Gaps,Job Ready\n");

        for (CandidateRosterDTO c : roster) {
            sb.append(c.getUserId()).append(",");
            sb.append(escapeCsv(c.getFullName())).append(",");
            sb.append(escapeCsv(c.getEmail())).append(",");
            sb.append(escapeCsv(c.getDepartment())).append(",");
            sb.append(escapeCsv(c.getTargetRoleName())).append(",");
            sb.append(c.getScore()).append("%,");
            sb.append(c.getAcquiredCount()).append("/").append(c.getTotalRequiredCount()).append(",");
            sb.append(c.getMissingCoreCount()).append(",");
            sb.append(c.isJobReady() ? "YES" : "NO").append("\n");
        }

        return sb.toString();
    }

    private String resolveDepartment(User user) {
        if (user.getDepartment() != null && !user.getDepartment().isBlank()) {
            return user.getDepartment().trim();
        }
        if (user.getEducation() != null) {
            String edu = user.getEducation().toLowerCase();
            if (edu.contains("computer") || edu.contains("cse") || edu.contains("cs")) return "Computer Science";
            if (edu.contains("information") || edu.contains("it")) return "Information Technology";
            if (edu.contains("electronic") || edu.contains("ece")) return "Electronics & Comm.";
            if (edu.contains("data")) return "Data Science";
            if (edu.contains("mech")) return "Mechanical";
        }
        return "Computer Science";
    }

    private String escapeCsv(String value) {
        if (value == null) return "\"\"";
        String escaped = value.replace("\"", "\"\"");
        return "\"" + escaped + "\"";
    }
}
