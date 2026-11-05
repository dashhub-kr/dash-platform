package com.ssafy.dash.common.encrypt;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AesEncryptorTest {

    private static final String KEY = "test-encrypt-secret-key-0123456789abcdef";

    private AesEncryptor encryptor;
    private AesEncryptor previousInstance;

    @BeforeEach
    void setUp() {
        previousInstance = AesEncryptor.getInstance();
        encryptor = newEncryptor(KEY);
    }

    @AfterEach
    void tearDown() {
        ReflectionTestUtils.setField(AesEncryptor.class, "instance", previousInstance);
    }

    @Test
    @DisplayName("암호화한 값을 복호화하면 원문이 나온다")
    void 암호화한_값을_복호화하면_원문이_나온다() {
        String encrypted = encryptor.encrypt("gho_token");

        assertThat(encrypted).startsWith("v2:").doesNotContain("gho_token");
        assertThat(encryptor.decrypt(encrypted)).isEqualTo("gho_token");
    }

    @Test
    @DisplayName("같은 원문도 매번 다른 암호문이 나온다")
    void 같은_원문도_매번_다른_암호문이_나온다() {
        assertThat(encryptor.encrypt("same")).isNotEqualTo(encryptor.encrypt("same"));
    }

    @Test
    @DisplayName("변조된 암호문은 복호화에 실패한다")
    void 변조된_암호문은_복호화에_실패한다() {
        String encrypted = encryptor.encrypt("gho_token");
        byte[] raw = Base64.getDecoder().decode(encrypted.substring(3));
        raw[raw.length - 1] ^= 1;
        String tampered = "v2:" + Base64.getEncoder().encodeToString(raw);

        assertThatThrownBy(() -> encryptor.decrypt(tampered))
                .isInstanceOf(AesEncryptor.CryptoException.class);
    }

    @Test
    @DisplayName("다른 키로는 복호화에 실패한다")
    void 다른_키로는_복호화에_실패한다() {
        String encrypted = encryptor.encrypt("gho_token");
        AesEncryptor other = newEncryptor("another-encrypt-secret-key-0123456789ab");

        assertThatThrownBy(() -> other.decrypt(encrypted))
                .isInstanceOf(AesEncryptor.CryptoException.class);
    }

    @Test
    @DisplayName("이전 CBC 형식도 복호화한다")
    void 이전_CBC_형식도_복호화한다() throws Exception {
        byte[] key = MessageDigest.getInstance("SHA-256").digest(KEY.getBytes(StandardCharsets.UTF_8));
        byte[] iv = new byte[16];
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"), new IvParameterSpec(iv));
        byte[] body = cipher.doFinal("legacy".getBytes(StandardCharsets.UTF_8));
        byte[] combined = new byte[iv.length + body.length];
        System.arraycopy(body, 0, combined, iv.length, body.length);

        assertThat(encryptor.decrypt(Base64.getEncoder().encodeToString(combined))).isEqualTo("legacy");
    }

    @Test
    @DisplayName("평문 허용 복호화는 평문은 그대로, 암호문은 대체 문구를 반환한다")
    void 평문_허용_복호화는_평문은_그대로_암호문은_대체문구를_반환한다() {
        String foreign = newEncryptor("another-encrypt-secret-key-0123456789ab").encrypt("secret");

        assertThat(encryptor.decryptAllowingPlaintext("안녕하세요", "X")).isEqualTo("안녕하세요");
        assertThat(encryptor.decryptAllowingPlaintext(foreign, "X")).isEqualTo("X");
    }

    @Test
    @DisplayName("키가 짧으면 초기화에 실패한다")
    void 키가_짧으면_초기화에_실패한다() {
        assertThatThrownBy(() -> newEncryptor("short"))
                .isInstanceOf(IllegalStateException.class);
    }

    private static AesEncryptor newEncryptor(String key) {
        AesEncryptor encryptor = new AesEncryptor();
        ReflectionTestUtils.setField(encryptor, "secretKey", key);
        encryptor.init();
        return encryptor;
    }
}
