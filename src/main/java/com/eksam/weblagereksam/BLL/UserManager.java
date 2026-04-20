package com.eksam.weblagereksam.BLL;

import com.eksam.weblagereksam.BE.User;
import com.eksam.weblagereksam.DAL.IUserDAO;
import com.eksam.weblagereksam.DAL.UserDAO;
import com.eksam.weblagereksam.GUI.Login.PasswordHasher;

import java.util.List;

public class UserManager {

    private final IUserDAO userDAO;

    public UserManager() {
        this.userDAO = new UserDAO();
    }

    public User login(String username, String password) {

        if (username == null || username.isBlank()) return null;
        if (password == null || password.length() < 4) return null;

        User user = userDAO.getUserByUsername(username);

        if (user == null) return null;

        boolean valid = PasswordHasher.verify(user.getPassword(), password);

        if (!valid) return null;

        return user;
    }

    public int addUser(String username, String password, String name, String email, String phoneNumber, int role) {

        if (username == null || username.isBlank()) return -1;
        if (password == null || password.length() < 4) return -1;

        String hashed = PasswordHasher.hash(password);

        return userDAO.addUser(username, hashed, name, email, phoneNumber, role);
    }

    public User getUserById(int id) {
        return userDAO.getUserById(id);
    }

    public List<User> getAllUsers() {
        return userDAO.getAllUsers();
    }

    public boolean deleteUser(int userId) {
        return userDAO.deleteUser(userId);
    }

    public boolean updateUser(int id, String username, String name,
                              String email, String phone,
                              String passwordHash, int role) {
        return userDAO.updateUser(id, username, name, email, phone, passwordHash, role);
    }
}