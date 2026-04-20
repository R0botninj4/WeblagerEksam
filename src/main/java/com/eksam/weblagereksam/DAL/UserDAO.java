package com.eksam.weblagereksam.DAL;



import com.eksam.weblagereksam.BE.User;
import com.eksam.weblagereksam.DAL.DB.DBConnector;
import com.eksam.weblagereksam.DAL.IUserDAO;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO implements IUserDAO {

    private final DBConnector dbConnector;

    public UserDAO() {
        try {
            dbConnector = new DBConnector();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public User login(String username, String password) {
        String sql = "SELECT * FROM Users WHERE Username=? AND PasswordHash=?";
        try (Connection conn = dbConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new User(
                        rs.getInt("UserID"),
                        rs.getString("Username"),
                        rs.getString("Name"),
                        rs.getString("Email"),
                        rs.getString("PhoneNumber"),
                        rs.getInt("RoleInt")
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public User getUserById(int id) {
        String sql = "SELECT * FROM Users WHERE UserID=?";
        try (Connection conn = dbConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new User(
                        rs.getInt("UserID"),
                        rs.getString("Username"),
                        rs.getString("Name"),
                        rs.getString("Email"),
                        rs.getString("PhoneNumber"),
                        rs.getInt("RoleInt")
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM Users";

        try (Connection conn = dbConnector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                users.add(new User(
                        rs.getInt("UserID"),
                        rs.getString("Username"),
                        rs.getString("Name"),
                        rs.getString("Email"),
                        rs.getString("PhoneNumber"),
                        rs.getInt("RoleInt")
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    @Override
    public int addUser(String username, String password, String name, String email, String phoneNumber, int role) {
        String roleText = switch (role) {
            case 1 -> "Admin";
            case 2 -> "Coordinator";
            default -> "Customer";
        };

        String sql = "INSERT INTO Users (Username, Name, Email, PhoneNumber, PasswordHash, RoleInt, Role) VALUES (?,?,?,?,?,?,?)";

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, username);
            stmt.setString(2, name);
            stmt.setString(3, email);
            stmt.setString(4, phoneNumber);
            stmt.setString(5, password);
            stmt.setInt(6, role);
            stmt.setString(7, roleText);

            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public boolean deleteUser(int userId) {
        String sql = "DELETE FROM Users WHERE UserID=?";
        try (Connection conn = dbConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            int affected = stmt.executeUpdate();
            return affected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean updateUser(int userId, String username, String name, String email, String phoneNumber, String passwordHash, int role) {
        String roleText = switch (role) {
            case 1 -> "Admin";
            case 2 -> "Coordinator";
            default -> "Customer";
        };

        String sql = "UPDATE Users SET Username=?, Name=?, Email=?, PhoneNumber=?, PasswordHash=?, RoleInt=?, Role=? WHERE UserID=?";
        try (Connection conn = dbConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, name);
            stmt.setString(3, email);
            stmt.setString(4, phoneNumber);
            stmt.setString(5, passwordHash);
            stmt.setInt(6, role);
            stmt.setString(7, roleText);
            stmt.setInt(8, userId);

            int affected = stmt.executeUpdate();
            return affected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public User getUserByUsername(String username) {
        String sql = "SELECT * FROM Users WHERE Username=?";

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new User(
                        rs.getInt("UserID"),
                        rs.getString("Username"),
                        rs.getString("PasswordHash"),
                        rs.getString("Name"),
                        rs.getString("Email"),
                        rs.getString("phoneNumber"),
                        rs.getInt("RoleInt")
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }


}