package com.eksam.weblagereksam.BE;

import java.util.UUID;
import java.time.LocalDateTime;

public class User {

    private UUID id;
    private String username;
    private String passwordHash;
    private UUID roleId;
    private String roleName;
    private boolean active;
    private LocalDateTime lastLogin;

    public User(UUID id,
                String username,
                String passwordHash,
                UUID roleId,
                String roleName,
                boolean active) {
        this(id, username, passwordHash, roleId, roleName, active, null);
    }

    public User(UUID id,
                String username,
                String passwordHash,
                UUID roleId,
                String roleName,
                boolean active,
                LocalDateTime lastLogin) {

        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.roleId = roleId;
        this.roleName = roleName;
        this.active = active;
        this.lastLogin = lastLogin;
    }

    public UUID getId() { return id; }

    public String getUsername() { return username; }

    public String getPasswordHash() { return passwordHash; }

    public UUID getRoleId() { return roleId; }

    public String getRoleName() { return roleName; }

    public boolean isActive() { return active; }

    public LocalDateTime getLastLogin() { return lastLogin; }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setRoleId(UUID roleId) {
        this.roleId = roleId;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
