package com.eksam.weblagereksam.GUI.Login;

import com.eksam.weblagereksam.BE.User;

public class Session {

    // ===== Current login state =====

    private static User currentUser;

    public static void setUser(User user) {
        currentUser = user;
    }

    public static User getUser() {
        return currentUser;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static void clear() {
        currentUser = null;
    }
}
