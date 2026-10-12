package com.skillforge.dto;

public class HeatmapCellDTO {

    private String department;
    private Integer roleId;
    private String roleName;
    private int studentCount;
    private int averageScore;
    private int jobReadyCount;
    private double jobReadyRate;

    public HeatmapCellDTO() {
    }

    public HeatmapCellDTO(String department, Integer roleId, String roleName, int studentCount,
                          int averageScore, int jobReadyCount, double jobReadyRate) {
        this.department = department;
        this.roleId = roleId;
        this.roleName = roleName;
        this.studentCount = studentCount;
        this.averageScore = averageScore;
        this.jobReadyCount = jobReadyCount;
        this.jobReadyRate = jobReadyRate;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
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

    public int getStudentCount() {
        return studentCount;
    }

    public void setStudentCount(int studentCount) {
        this.studentCount = studentCount;
    }

    public int getAverageScore() {
        return averageScore;
    }

    public void setAverageScore(int averageScore) {
        this.averageScore = averageScore;
    }

    public int getJobReadyCount() {
        return jobReadyCount;
    }

    public void setJobReadyCount(int jobReadyCount) {
        this.jobReadyCount = jobReadyCount;
    }

    public double getJobReadyRate() {
        return jobReadyRate;
    }

    public void setJobReadyRate(double jobReadyRate) {
        this.jobReadyRate = jobReadyRate;
    }
}
