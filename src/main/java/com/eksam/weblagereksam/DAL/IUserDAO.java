package com.eksam.weblagereksam.DAL;

import com.eksam.weblagereksam.BE.User;

import java.util.List;

public interface IUserDAO {

    User getUserByUsername(String username);

    User login(String username, String password);

    User getUserById(int id);

    List<User> getAllUsers();

    boolean deleteUser(int userId);

    boolean updateUser(
            int userId,
            String username,
            String name,
            String email,
            String phoneNumber,
            String passwordHash,
            int role
    );

    int addUser(
            String username,
            String passwordHash,
            String name,
            String email,
            String phoneNumber,
            int role
    );
}