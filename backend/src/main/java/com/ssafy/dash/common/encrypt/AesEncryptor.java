package com.ssafy.dash.common.encrypt;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import jakarta.annotation.PostConstruct;

@Component
public class AesEncryptor {

    @Value("${dash.encrypt.secret-key:default_secret_key_for_dev_env_32b!}")
    private String secretKey;

    private SecretKeySpec secretKeySpec;

    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static AesEncryptor instance;

    @PostConstruct
    public void init() {
        try {
            // 키 길이를 32바이트로 맞추기 위해 SHA-256 해싱
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] keyBytes = digest.digest(secretKey.getBytes(StandardCharsets.UTF_8));
            this.secretKeySpec = new SecretKeySpec(keyBytes, "AES");
            instance = this;
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize AesEncryptor", e);
        }
    }

    public static AesEncryptor getInstance() {
        return instance;
    }

    public String encrypt(String plainText) {
        if (plainText == null)
            return null;
        try {
            // 랜덤 IV(Initial Vector) 생성
            byte[] iv = new byte[16];
            new SecureRandom().nextBytes(iv);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivSpec);

            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            // IV + 암호화된 데이터 결합
            byte[] combined = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);

            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            throw new RuntimeException("Failed to encrypt data", e);
        }
    }

    public String decrypt(String cipherText) {
        if (cipherText == null)
            return null;
        try {
            byte[] combined = Base64.getDecoder().decode(cipherText);

            // IV(초기화 벡터) 추출
            byte[] iv = new byte[16];
            System.arraycopy(combined, 0, iv, 0, iv.length);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            // 암호화된 데이터 부분 추출
            byte[] encrypted = new byte[combined.length - iv.length];
            System.arraycopy(combined, iv.length, encrypted, 0, encrypted.length);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivSpec);

            byte[] decoded = cipher.doFinal(encrypted);
            return new String(decoded, StandardCharsets.UTF_8);
        } catch (Exception e) {
            // 복호화 실패 시 원본 반환
            return cipherText;
        }
    }
}
