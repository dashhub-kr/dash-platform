package com.ssafy.dash.common.encrypt;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

/**
 * DB 저장 데이터 암호화 (AES-256-GCM)
 *
 * <p>암호문 형식: {@code v2:Base64(IV 12바이트 + 암호문 + 인증 태그)}.
 * 접두사가 없는 값은 이전 형식(AES-CBC)으로 간주해 복호화만 지원한다.
 */
@Component
public class AesEncryptor {

    private static final String VERSION_PREFIX = "v2:";
    private static final String GCM_ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_BITS = 128;

    private static final String LEGACY_CBC_ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final int LEGACY_IV_LENGTH = 16;

    private static final int MIN_SECRET_KEY_LENGTH = 32;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private static AesEncryptor instance;

    @Value("${dash.encrypt.secret-key:}")
    private String secretKey;

    private SecretKeySpec secretKeySpec;

    @PostConstruct
    public void init() {
        if (secretKey == null || secretKey.isBlank() || secretKey.length() < MIN_SECRET_KEY_LENGTH) {
            throw new IllegalStateException(
                    "dash.encrypt.secret-key(DASH_ENCRYPT_SECRET_KEY)를 " + MIN_SECRET_KEY_LENGTH + "자 이상으로 설정해야 합니다.");
        }
        try {
            // 키 길이를 32바이트로 맞추기 위해 SHA-256 해싱
            byte[] keyBytes = MessageDigest.getInstance("SHA-256")
                    .digest(secretKey.getBytes(StandardCharsets.UTF_8));
            this.secretKeySpec = new SecretKeySpec(keyBytes, "AES");
            instance = this;
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Failed to initialize AesEncryptor", e);
        }
    }

    public static AesEncryptor getInstance() {
        return instance;
    }

    public String encrypt(String plainText) {
        if (plainText == null) {
            return null;
        }
        try {
            byte[] iv = new byte[GCM_IV_LENGTH];
            SECURE_RANDOM.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(GCM_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, new GCMParameterSpec(GCM_TAG_BITS, iv));
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            byte[] combined = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);

            return VERSION_PREFIX + Base64.getEncoder().encodeToString(combined);
        } catch (GeneralSecurityException e) {
            throw new CryptoException("Failed to encrypt data", e);
        }
    }

    /**
     * 복호화한다. 키가 맞지 않거나 변조된 데이터는 {@link CryptoException}을 던진다.
     */
    public String decrypt(String cipherText) {
        if (cipherText == null) {
            return null;
        }
        try {
            if (cipherText.startsWith(VERSION_PREFIX)) {
                return decryptGcm(Base64.getDecoder().decode(cipherText.substring(VERSION_PREFIX.length())));
            }
            return decryptLegacyCbc(Base64.getDecoder().decode(cipherText));
        } catch (GeneralSecurityException | IllegalArgumentException e) {
            throw new CryptoException("Failed to decrypt data", e);
        }
    }

    /**
     * 암호화 도입 전 평문 데이터와 섞인 컬럼용. 암호문 형태인데 복호화에 실패하면
     * 원문 대신 {@code fallback}을 반환해 암호문이 노출되지 않게 한다.
     */
    public String decryptAllowingPlaintext(String value, String fallback) {
        if (value == null) {
            return null;
        }
        try {
            return decrypt(value);
        } catch (CryptoException e) {
            return looksEncrypted(value) ? fallback : value;
        }
    }

    private boolean looksEncrypted(String value) {
        if (value.startsWith(VERSION_PREFIX)) {
            return true;
        }
        try {
            byte[] decoded = Base64.getDecoder().decode(value);
            int bodyLength = decoded.length - LEGACY_IV_LENGTH;
            return bodyLength > 0 && bodyLength % 16 == 0;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private String decryptGcm(byte[] combined) throws GeneralSecurityException {
        if (combined.length <= GCM_IV_LENGTH) {
            throw new IllegalArgumentException("ciphertext too short");
        }
        byte[] iv = Arrays.copyOfRange(combined, 0, GCM_IV_LENGTH);
        byte[] encrypted = Arrays.copyOfRange(combined, GCM_IV_LENGTH, combined.length);

        Cipher cipher = Cipher.getInstance(GCM_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, new GCMParameterSpec(GCM_TAG_BITS, iv));

        return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
    }

    private String decryptLegacyCbc(byte[] combined) throws GeneralSecurityException {
        if (combined.length <= LEGACY_IV_LENGTH) {
            throw new IllegalArgumentException("ciphertext too short");
        }
        byte[] iv = Arrays.copyOfRange(combined, 0, LEGACY_IV_LENGTH);
        byte[] encrypted = Arrays.copyOfRange(combined, LEGACY_IV_LENGTH, combined.length);

        Cipher cipher = Cipher.getInstance(LEGACY_CBC_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, new IvParameterSpec(iv));

        return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
    }

    public static class CryptoException extends RuntimeException {
        public CryptoException(String message, Throwable cause) {
            super(message, cause);
        }
    }

}
