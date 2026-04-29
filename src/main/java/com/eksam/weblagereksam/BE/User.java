package com.eksam.weblagereksam.BE;

import java.util.UUID;
import java.time.LocalDateTime;

public class User {

    private UUID id;
    private String username;
    private String passwordHash;
    private String fullName;
    private UUID roleId;
    private String roleName;
    private boolean active;
    private LocalDateTime lastLogin;

    public User(UUID id,
                String username,
                String passwordHash,
                String fullName,
                UUID roleId,
                String roleName,
                boolean active) {
        this(id, username, passwordHash, fullName, roleId, roleName, active, null);
    }

    public User(UUID id,
                String username,
                String passwordHash,
                String fullName,
                UUID roleId,
                String roleName,
                boolean active,
                LocalDateTime lastLogin) {

        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.roleId = roleId;
        this.roleName = roleName;
        this.active = active;
        this.lastLogin = lastLogin;
    }

    public UUID getId() { return id; }

    public String getUsername() { return username; }

    public String getPasswordHash() { return passwordHash; }

    public String getFullName() { return fullName; }

    public UUID getRoleId() { return roleId; }

    public String getRoleName() { return roleName; }

    public boolean isActive() { return active; }

    public LocalDateTime getLastLogin() { return lastLogin; }

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
