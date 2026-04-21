package com.eksam.weblagereksam.BE;

import java.util.UUID;

public class User {

    private UUID id;
    private String username;
    private String passwordHash;
    private String fullName;
    private UUID roleId;
    private String roleName;
    private boolean active;

    public User(UUID id,
                String username,
                String passwordHash,
                String fullName,
                UUID roleId,
                String roleName,
                boolean active) {

        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.roleId = roleId;
        this.roleName = roleName;
        this.active = active;
    }

    public UUID getId() { return id; }

    public String getUsername() { return username; }

    public String getPasswordHash() { return passwordHash; }

    public String getFullName() { return fullName; }

    public UUID getRoleId() { return roleId; }

    public String getRoleName() { return roleName; }

    public boolean isActive() { return active; }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setRoleId(UUID roleId) {
        this.roleId = roleId;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}