package com.eksam.weblagereksam.DAL;

import com.eksam.weblagereksam.BE.User;
import com.eksam.weblagereksam.BE.Role;
import com.eksam.weblagereksam.BE.UserActivity;

import java.util.List;
import java.util.UUID;

public interface IUserDAO {

    User getUserByUsername(String username);

    User getUserById(UUID id);

    List<User> getAllUsers();

    List<UserActivity> getUserActivities();

    List<Role> getAllRoles();

    UUID addUser(String username, String passwordHash, UUID roleId);

    boolean updateUser(User user);

    boolean updateLastLogin(UUID id);
}
