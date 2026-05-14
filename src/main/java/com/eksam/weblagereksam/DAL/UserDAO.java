package com.eksam.weblagereksam.DAL;

import com.eksam.weblagereksam.BE.User;
import com.eksam.weblagereksam.BE.Role;
import com.eksam.weblagereksam.DAL.DB.DBConnector;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UserDAO implements IUserDAO {

    private final DBConnector dbConnector;

    public UserDAO() throws Exception {
        dbConnector = new DBConnector();
    }


    // BLVDSIONVEFW
    public User getUserByUsername(String username) {

        String sql = """
            SELECT u.*, r.Name AS RoleName
            FROM Users u
            INNER JOIN Roles r ON u.RoleId = r.Id
            WHERE u.Username = ?
        """;

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapUser(rs);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }
    public User getUserById(UUID id) {

        String sql = """
            SELECT u.*, r.Name AS RoleName
            FROM Users u
            INNER JOIN Roles r ON u.RoleId = r.Id
            WHERE u.Id = ?
        """;

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id.toString());

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapUser(rs);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }
    public List<User> getAllUsers() {

        List<User> users = new ArrayList<>();

        String sql = """
            SELECT u.*, r.Name AS RoleName
            FROM Users u
            INNER JOIN Roles r ON u.RoleId = r.Id
            ORDER BY Username
        """;

        try (Connection conn = dbConnector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                users.add(mapUser(rs));
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return users;
    }
    public List<Role> getAllRoles() {

        List<Role> roles = new ArrayList<>();

        String sql = """
            SELECT *
            FROM Roles
            ORDER BY Name
        """;

        try (Connection conn = dbConnector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                roles.add(new Role(
                        UUID.fromString(rs.getString("Id")),
                        rs.getString("Name")
                ));
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return roles;
    }
    public UUID addUser(String username,
                        String passwordHash,
                        UUID roleId) {

        String sql = """
            INSERT INTO Users
            (Username, PasswordHash, RoleId)
            OUTPUT INSERTED.Id
            VALUES (?, ?, ?)
        """;

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, passwordHash);
            stmt.setString(3, roleId.toString());

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return UUID.fromString(rs.getString(1));
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }
    public boolean updateUser(User user) {

        String sql = """
            UPDATE Users
            SET Username = ?,
                RoleId = ?,
                IsActive = ?
            WHERE Id = ?
        """;

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getRoleId().toString());
            stmt.setBoolean(3, user.isActive());
            stmt.setString(4, user.getId().toString());

            return stmt.executeUpdate() > 0;

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return false;
    }
    public boolean updateLastLogin(UUID id) {

        String sql = """
            UPDATE Users
            SET LastLogin = GETDATE()
            WHERE Id = ?
        """;

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id.toString());
            return stmt.executeUpdate() > 0;

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return false;
    }

    private User mapUser(ResultSet rs) throws SQLException {
        Timestamp lastLoginTimestamp = rs.getTimestamp("LastLogin");

        return new User(
                UUID.fromString(rs.getString("Id")),
                rs.getString("Username"),
                rs.getString("PasswordHash"),
                UUID.fromString(rs.getString("RoleId")),
                rs.getString("RoleName"),
                rs.getBoolean("IsActive"),
                lastLoginTimestamp != null ? lastLoginTimestamp.toLocalDateTime() : null
        );
    }
}
