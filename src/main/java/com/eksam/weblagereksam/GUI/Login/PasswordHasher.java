package com.eksam.weblagereksam.GUI.Login;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;

/**
 * Helper for hashing and verifying passwords with Argon2.
 *
 * Note: this is placed under GUI/Login in the current project, but conceptually
 * password hashing belongs close to login/business logic.
 */
public class PasswordHasher {

    // ===== Argon2 setup =====

    private static final Argon2 argon2 =
            Argon2Factory.create();

    // ===== Public hashing methods =====

    public static String hash(String password) {

        return argon2.hash(
                3,
                65536,
                1,
                password.toCharArray()
        );
    }

    public static boolean verify(String hash,
                                 String password) {

        // Null input is treated as an invalid login instead of throwing an exception.
        if (hash == null || password == null)
            return false;

        return argon2.verify(
                hash,
                password.toCharArray()
        );
    }
}
