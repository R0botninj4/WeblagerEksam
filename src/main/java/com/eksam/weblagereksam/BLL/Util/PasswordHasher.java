package com.eksam.weblagereksam.BLL.Util;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;

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

        if (hash == null || password == null)
            return false;

        return argon2.verify(
                hash,
                password.toCharArray()
        );
    }
}
