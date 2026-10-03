package com.example.just5minbackend.util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * 密码哈希工具：PBKDF2WithHmacSHA256（JDK 内置，无需引入 spring-security-crypto）。
 * <p>存储格式：{@code pbkdf2-sha256$<迭代次数>$<base64盐>$<base64摘要>}，
 * 盐随机、每次哈希都不同，校验用 {@link MessageDigest#isEqual} 做定时安全比较。</p>
 */
public final class PasswordUtil {

    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final String PREFIX = "pbkdf2-sha256";
    private static final String SEPARATOR = "$";
    private static final int ITERATIONS = 100_000;
    private static final int SALT_BYTES = 16;
    private static final int KEY_BITS = 256;

    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordUtil() {
    }

    /**
     * 生成密码哈希。
     */
    public static String hash(String rawPassword) {
        if (rawPassword == null || rawPassword.isEmpty()) {
            throw new IllegalArgumentException("密码不能为空");
        }
        byte[] salt = new byte[SALT_BYTES];
        RANDOM.nextBytes(salt);
        byte[] digest = pbkdf2(rawPassword.toCharArray(), salt, ITERATIONS);
        Base64.Encoder encoder = Base64.getEncoder();
        return PREFIX + SEPARATOR + ITERATIONS + SEPARATOR
                + encoder.encodeToString(salt) + SEPARATOR + encoder.encodeToString(digest);
    }

    /**
     * 校验密码。任何格式异常都返回 false，不抛异常，避免泄露内部细节。
     */
    public static boolean matches(String rawPassword, String stored) {
        if (rawPassword == null || rawPassword.isEmpty() || stored == null || stored.isEmpty()) {
            return false;
        }
        String[] parts = stored.split("\\" + SEPARATOR);
        if (parts.length != 4 || !PREFIX.equals(parts[0])) {
            return false;
        }
        try {
            int iterations = Integer.parseInt(parts[1]);
            Base64.Decoder decoder = Base64.getDecoder();
            byte[] salt = decoder.decode(parts[2]);
            byte[] expected = decoder.decode(parts[3]);
            byte[] actual = pbkdf2(rawPassword.toCharArray(), salt, iterations);
            return MessageDigest.isEqual(expected, actual);
        } catch (RuntimeException e) {
            return false;
        }
    }

    private static byte[] pbkdf2(char[] password, byte[] salt, int iterations) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, KEY_BITS);
            return SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).getEncoded();
        } catch (Exception e) {
            throw new IllegalStateException("密码哈希计算失败", e);
        }
    }
}
