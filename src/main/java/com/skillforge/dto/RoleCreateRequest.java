package com.skillforge.dto;

import jakarta.validation.constraints.NotBlank;

public class RoleCreateRequest {

    @NotBlank(message = "Role name is required")
    private String roleName;

    private String description;

    public RoleCreateRequest() {
    }

    public RoleCreateRequest(String roleName, String description) {
        this.roleName = roleName;
        this.description = description;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
