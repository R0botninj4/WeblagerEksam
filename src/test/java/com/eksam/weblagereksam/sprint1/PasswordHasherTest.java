package com.eksam.weblagereksam.sprint1;

import com.eksam.weblagereksam.BLL.Security.PasswordHasher;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordHasherTest {

    @Test
    void sprint1Login_passwordCanBeHashedAndVerified() {
        String hash = PasswordHasher.hash("GOD12");

        assertNotNull(hash);
        assertNotEquals("GOD12", hash);
        assertTrue(PasswordHasher.verify(hash, "GOD12"));
    }

    @Test
    void sprint1Login_wrongPasswordIsRejected() {
        String hash = PasswordHasher.hash("GOD12");

        assertFalse(PasswordHasher.verify(hash, "wrong-password"));
    }

    @Test
    void sprint1Login_nullInputIsRejected() {
        assertFalse(PasswordHasher.verify(null, "GOD12"));
        assertFalse(PasswordHasher.verify("hash", null));
    }
}
