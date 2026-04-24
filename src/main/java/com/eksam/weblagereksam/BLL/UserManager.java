package com.eksam.weblagereksam.BLL;

import com.eksam.weblagereksam.BE.User;
import com.eksam.weblagereksam.DAL.IUserDAO;
import com.eksam.weblagereksam.DAL.UserDAO;
import com.eksam.weblagereksam.GUI.Login.PasswordHasher;

import java.util.List;
import java.util.UUID;

/**
 * BLL manager for user login and user CRUD.
 *
 * Password checks happen here so the GUI does not know how hashing works.
 */
public class UserManager {

    // ===== DAL dependency =====

    private final IUserDAO userDAO;

    public UserManager() throws Exception {
        userDAO = new UserDAO();
    }

    // ===== Login flow =====

    public User login(String username, String password) {

        if (username == null || username.isBlank()) return null;
        if (password == null || password.isBlank()) return null;

        User user = userDAO.getUserByUsername(username);

        if (user == null) return null;

        boolean valid =
                PasswordHasher.verify(
                        user.getPasswordHash(),
                        password
                );

        if (!valid) return null;

        if (!user.isActive()) return null;

        return user;
    }

    // ===== CRUD methods =====

    public UUID createUser(String username,
                           String password,
                           String fullName,
                           UUID roleId) {

        // Store only the password hash, never the plain password.
        String hash = PasswordHasher.hash(password);

        return userDAO.addUser(
                username,
                hash,
                fullName,
                roleId
        );
    }

    public List<User> getAllUsers() {
        return userDAO.getAllUsers();
    }

    public User getUser(UUID id) {
        return userDAO.getUserById(id);
    }

    public boolean updateUser(User user) {
        return userDAO.updateUser(user);
    }

    public boolean deleteUser(UUID id) {
        return userDAO.deleteUser(id);
    }
}
