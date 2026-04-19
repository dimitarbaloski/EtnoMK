package mk.ukim.finki.etnomk.security;

import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtHelperTest {

    private final JwtHelper jwtHelper = new JwtHelper("7ac51f34954d17b2c22a1a95ea44f1f61c72a0b87d186c8e2c607edb4db99ffd");

    @Test
    void generatedTokenContainsUsernameAndValidatesForSameUser() {
        User user = new User("stefan", "password", List.of(() -> "ROLE_USER"));

        String token = jwtHelper.generateToken(user);

        assertEquals("stefan", jwtHelper.extractUsername(token));
        assertTrue(jwtHelper.isValid(token, user));
    }

    @Test
    void tamperedTokenIsRejected() {
        User user = new User("stefan", "password", List.of(() -> "ROLE_USER"));
        String token = jwtHelper.generateToken(user);
        String tamperedToken = token.substring(0, token.length() - 2) + "aa";

        assertThrows(JwtException.class, () -> jwtHelper.extractUsername(tamperedToken));
    }
}
