package com.eksam.weblagereksam.BLL.Manager;

import com.eksam.weblagereksam.BE.User;
import com.eksam.weblagereksam.BE.Role;
import com.eksam.weblagereksam.BLL.Security.PasswordHasher;
import com.eksam.weblagereksam.DAL.IUserDAO;
import com.eksam.weblagereksam.DAL.UserDAO;

import java.util.List;
import java.util.UUID;

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

        userDAO.updateLastLogin(user.getId());

        return user;
    }

    // ===== CRUD methods =====

    public UUID createUser(String username,
                           String password,
                           String fullName,
                           UUID roleId) {

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

    public List<Role> getAllRoles() {
        return userDAO.getAllRoles();
    }

    public User getUser(UUID id) {
        return userDAO.getUserById(id);
    }

    public boolean updateUser(User user) {
        return userDAO.updateUser(user);
    }

    public boolean deactivateUser(UUID id) {
        User user = userDAO.getUserById(id);

        if (user == null) {
            return false;
        }

        user.setActive(false);
        return userDAO.updateUser(user);
    }

}
