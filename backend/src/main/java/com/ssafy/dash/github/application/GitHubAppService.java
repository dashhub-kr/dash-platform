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
import org.springframework.web.client.RestTemplate;

@Service
public class GitHubAppService {

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
            // JWT 유효기간: 10분
            long exp = now + (10 * 60);

            return Jwts.builder()
                    .issuedAt(new Date(now * 1000))
                    .expiration(new Date(exp * 1000))
                    .issuer(properties.getId())
                    .signWith(loadPrivateKey())
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
            ResponseEntity<Map> response = restTemplate.exchange(
                    "https://api.github.com/app/installations/" + installationId + "/access_tokens",
                    HttpMethod.POST,
                    entity,
                    Map.class);

            return (String) response.getBody().get("token");
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve installation access token", e);
        }
    }

    private PrivateKey loadPrivateKey() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        String privateKeyContent = loadPrivateKeyContent();

        // PEM 헤더/푸터 제거 및 줄바꿈 제거
        String privateKeyPEM = privateKeyContent
                .replace("-----BEGIN RSA PRIVATE KEY-----", "")
                .replace("-----END RSA PRIVATE KEY-----", "")
                .replaceAll("\\s", "");

        byte[] encoded = Base64.getDecoder().decode(privateKeyPEM);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(encoded));
    }

    private String loadPrivateKeyContent() throws IOException {
        try (Reader reader = new InputStreamReader(
                resourceLoader.getResource(properties.getPrivateKeyPath()).getInputStream(),
                StandardCharsets.UTF_8)) {
            return FileCopyUtils.copyToString(reader);
        }
    }
}
