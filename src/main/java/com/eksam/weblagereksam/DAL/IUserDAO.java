package com.eksam.weblagereksam.DAL;

import com.eksam.weblagereksam.BE.User;

import java.util.List;
import java.util.UUID;

public interface IUserDAO {

    User getUserByUsername(String username);

    User getUserById(UUID id);

    List<User> getAllUsers();

    UUID addUser(String username, String passwordHash, String fullName, UUID roleId);

    boolean updateUser(User user);

    boolean deleteUser(UUID id);
}
