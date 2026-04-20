package com.eksam.weblagereksam.GUI.Login;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;

public class PasswordHasher {

    private static final Argon2 argon2 = Argon2Factory.create();

    public static String hash(String password) {
        return argon2.hash(3, 65536, 1, password.toCharArray());
    }

    public static boolean verify(String hash, String password) {
        return argon2.verify(hash, password.toCharArray());
    }
}