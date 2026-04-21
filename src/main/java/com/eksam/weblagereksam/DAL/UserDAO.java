package com.eksam.weblagereksam.DAL;

import com.eksam.weblagereksam.BE.User;
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

    @Override
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

    @Override
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

    @Override
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

    @Override
    public UUID addUser(String username,
                        String passwordHash,
                        String fullName,
                        UUID roleId) {

        String sql = """
            INSERT INTO Users
            (Username, PasswordHash, FullName, RoleId)
            OUTPUT INSERTED.Id
            VALUES (?, ?, ?, ?)
        """;

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, passwordHash);
            stmt.setString(3, fullName);
            stmt.setString(4, roleId.toString());

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return UUID.fromString(rs.getString(1));
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    @Override
    public boolean updateUser(User user) {

        String sql = """
            UPDATE Users
            SET Username = ?,
                FullName = ?,
                RoleId = ?,
                IsActive = ?
            WHERE Id = ?
        """;

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getFullName());
            stmt.setString(3, user.getRoleId().toString());
            stmt.setBoolean(4, user.isActive());
            stmt.setString(5, user.getId().toString());

            return stmt.executeUpdate() > 0;

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean deleteUser(UUID id) {

        String sql = "DELETE FROM Users WHERE Id = ?";

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

        return new User(
                UUID.fromString(rs.getString("Id")),
                rs.getString("Username"),
                rs.getString("PasswordHash"),
                rs.getString("FullName"),
                UUID.fromString(rs.getString("RoleId")),
                rs.getString("RoleName"),
                rs.getBoolean("IsActive")
        );
    }
}