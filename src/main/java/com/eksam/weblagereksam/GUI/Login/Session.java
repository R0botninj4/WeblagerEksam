package com.eksam.weblagereksam.GUI.Login;

import com.eksam.weblagereksam.BE.User;

public class Session {

    private static User currentUser;

    public static void setUser(User user) {
        currentUser = user;
    }

    public static User getUser() {
        return currentUser;
    }

    public static void clear() {
        currentUser = null;
    }
}
