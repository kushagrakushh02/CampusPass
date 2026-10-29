package com.campuspass.util;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Password hashing with PBKDF2-HMAC-SHA256 and a random salt per user.
 * Stored format:  pbkdf2$iterations$salt$hash   (salt and hash are Base64).
 */
public final class PasswordUtil {

    private static final int ITERATIONS = 120_000;
    private static final int KEY_BITS = 256;
    private static final SecureRandom RANDOM = new SecureRandom();
    // burns the same time when an email does not exist, so response time does not reveal valid emails
    private static final String DUMMY = hash("not-a-real-password");

    private PasswordUtil() { }

    public static String hash(String password) {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        byte[] h = derive(password.toCharArray(), salt, ITERATIONS);
        Base64.Encoder b64 = Base64.getEncoder();
        return "pbkdf2$" + ITERATIONS + "$" + b64.encodeToString(salt) + "$" + b64.encodeToString(h);
    }

    /** Pass stored = null when the user does not exist: the check still runs, and always returns false. */
    public static boolean verify(String password, String stored) {
        if (password == null) return false;
        boolean unknownUser = (stored == null);
        String hashToCheck = unknownUser ? DUMMY : stored;
        try {
            String[] p = hashToCheck.split("\\$");
            if (p.length != 4 || !"pbkdf2".equals(p[0])) return false;
            int iterations = Integer.parseInt(p[1]);
            byte[] salt = Base64.getDecoder().decode(p[2]);
            byte[] expected = Base64.getDecoder().decode(p[3]);
            byte[] actual = derive(password.toCharArray(), salt, iterations);
            return MessageDigest.isEqual(expected, actual) && !unknownUser;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /** At least 8 characters with a letter and a digit. */
    public static boolean isStrongEnough(String password) {
        if (password == null || password.length() < 8) return false;
        boolean letter = false, digit = false;
        for (char c : password.toCharArray()) {
            if (Character.isLetter(c)) letter = true;
            if (Character.isDigit(c)) digit = true;
        }
        return letter && digit;
    }

    private static byte[] derive(char[] password, byte[] salt, int iterations) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, KEY_BITS);
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("PBKDF2 is not available on this JVM", e);
        }
    }
}
