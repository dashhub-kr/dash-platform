package com.ssafy.dash.github.application;

import com.ssafy.dash.github.config.GitHubAppProperties;
import io.jsonwebtoken.Jwts;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;

import java.util.Date;
import java.util.Map;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import org.springframework.http.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class GitHubAppService {

    private static final Logger log = LoggerFactory.getLogger(GitHubAppService.class);

    private final GitHubAppProperties properties;
    private final ResourceLoader resourceLoader;
    private final RestTemplate restTemplate;

    public GitHubAppService(GitHubAppProperties properties, ResourceLoader resourceLoader, RestTemplate restTemplate) {
        this.properties = properties;
        this.resourceLoader = resourceLoader;
        this.restTemplate = restTemplate;
    }

    public String generateJwt() {
        try {
            long now = Instant.now().getEpochSecond();
            // GitHub 서버와의 시계 오차로 인한 인증 실패를 방지하기 위해 발행 시간을 30초 앞당겨 설정
            long iat = now - 30;
            // JWT 유효기간: 10분
            long exp = now + (10 * 60);

            return Jwts.builder()
                    .issuedAt(new Date(iat * 1000))
                    .expiration(new Date(exp * 1000))
                    .issuer(properties.getId())
                    .signWith(loadPrivateKey(), Jwts.SIG.RS256)
                    .compact();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate GitHub App JWT", e);
        }
    }

    public String getInstallationAccessToken(long installationId) {
        String jwt = generateJwt();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + jwt);
        headers.set("Accept", "application/vnd.github+json");

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    "https://api.github.com/app/installations/" + installationId + "/access_tokens",
                    HttpMethod.POST,
                    entity,
                    new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {
                    });

            return (String) response.getBody().get("token");
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve installation access token", e);
        }
    }

    public boolean isAppInstalledOnRepo(String fullName) {
        String jwt = generateJwt();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + jwt);
        headers.set("Accept", "application/vnd.github+json");
        headers.set("User-Agent", "DashHub-App");

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            log.debug("Checking GitHub App installation for repo: {} using App ID: {}", fullName, properties.getId());
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    "https://api.github.com/repos/" + fullName + "/installation",
                    HttpMethod.GET,
                    entity,
                    new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {
                    });

            log.info("GitHub App installation check for {}: Status {}", fullName, response.getStatusCode());
            return response.getStatusCode().is2xxSuccessful();
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("GitHub App (ID: {}) is not installed on repository: {}", properties.getId(), fullName);
            return false;
        } catch (HttpClientErrorException e) {
            log.error("GitHub API error checking installation: {} - {}", e.getStatusCode(),
                    e.getResponseBodyAsString());
            return false;
        } catch (Exception e) {
            log.error("Unexpected error checking GitHub App installation for repository: {}", fullName, e);
            throw new RuntimeException("Failed to check GitHub App installation", e);
        }
    }

    private PrivateKey loadPrivateKey() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        String privateKeyContent = loadPrivateKeyContent();

        // PEM 헤더/푸터 제거 및 줄바꿈 제거
        String privateKeyPEM = privateKeyContent
                .replace("-----BEGIN RSA PRIVATE KEY-----", "")
                .replace("-----END RSA PRIVATE KEY-----", "")
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");

        byte[] encoded = Base64.getDecoder().decode(privateKeyPEM);

        // PKCS#1 형식(BEGIN RSA PRIVATE KEY)인 경우 PKCS#8로 변환 필요
        if (privateKeyContent.contains("RSA PRIVATE KEY")) {
            encoded = convertPkcs1ToPkcs8(encoded);
        }

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(encoded));
    }

    private byte[] convertPkcs1ToPkcs8(byte[] pkcs1Bytes) {
        int pkcs1Length = pkcs1Bytes.length;
        int totalLength = pkcs1Length + 22;
        byte[] pkcs8Header = new byte[] {
                0x30, (byte) 0x82, (byte) ((totalLength >> 8) & 0xff), (byte) (totalLength & 0xff), // Sequence
                0x02, 0x01, 0x00, // Version
                0x30, 0x0d, 0x06, 0x09, 0x2a, (byte) 0x86, 0x48, (byte) 0xf6, (byte) 0xf7, 0x0d, 0x01, 0x01, 0x01, 0x05,
                0x00, // Algorithm (RSA)
                0x04, (byte) 0x82, (byte) ((pkcs1Length >> 8) & 0xff), (byte) (pkcs1Length & 0xff) // Octet String
        };
        byte[] pkcs8Bytes = new byte[pkcs8Header.length + pkcs1Bytes.length];
        System.arraycopy(pkcs8Header, 0, pkcs8Bytes, 0, pkcs8Header.length);
        System.arraycopy(pkcs1Bytes, 0, pkcs8Bytes, pkcs8Header.length, pkcs1Bytes.length);
        return pkcs8Bytes;
    }

    private String loadPrivateKeyContent() throws IOException {
        try (Reader reader = new InputStreamReader(
                resourceLoader.getResource(properties.getPrivateKeyPath()).getInputStream(),
                StandardCharsets.UTF_8)) {
            return FileCopyUtils.copyToString(reader);
        }
    }
}
