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
            // JWT 유효기간: 10분
            long iat = now - 30;
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
