package com.ssafy.dash.github.infrastructure.client;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.dash.github.config.GitHubWebhookProperties;
import com.ssafy.dash.github.domain.GitHubClient;
import com.ssafy.dash.onboarding.domain.exception.WebhookRegistrationException;

@Component
public class GitHubClientImpl implements GitHubClient {

    private static final Logger log = LoggerFactory.getLogger(GitHubClientImpl.class);
    private static final String ROOT_URI = "https://api.github.com";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String callbackUrl;
    private final String secret;
    private final List<String> events;

    public GitHubClientImpl(
            RestTemplateBuilder restTemplateBuilder,
            ObjectMapper objectMapper,
            GitHubWebhookProperties properties) {
        this.restTemplate = restTemplateBuilder.rootUri(ROOT_URI).build();
        this.objectMapper = objectMapper;
        this.callbackUrl = properties.getCallbackUrl();
        this.secret = properties.getSecret();
        this.events = parseEvents(properties.getEvents());
    }

    @Override
    public void registerWebhook(String repositoryFullName, String accessToken) {
        RepositorySlug slug = RepositorySlug.from(repositoryFullName);
        validateConfiguration(accessToken);

        Map<String, Object> payload = Map.of(
                "name", "web",
                "active", Boolean.TRUE,
                "events", events,
                "config", Map.of(
                        "url", callbackUrl,
                        "content_type", "json",
                        "secret", secret,
                        "insecure_ssl", "0"));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setBearerAuth(accessToken);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(payload, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    "/repos/{owner}/{repo}/hooks",
                    HttpMethod.POST,
                    requestEntity,
                    String.class,
                    slug.owner(),
                    slug.repository());

            if (log.isDebugEnabled()) {
                log.debug("GitHub webhook create response status: {}", response.getStatusCode());
            }
        } catch (HttpClientErrorException.UnprocessableEntity ex) {
            String body = ex.getResponseBodyAsString();
            if (body != null && body.contains("Hook already exists")) {
                log.info("GitHub webhook already exists for {}/{}", slug.owner(), slug.repository());
                return;
            }
            throw new WebhookRegistrationException(resolveApiError(body, "GitHub 웹훅 생성에 실패했습니다."), ex);
        } catch (HttpClientErrorException ex) {
            throw new WebhookRegistrationException(resolveApiError(ex.getResponseBodyAsString(),
                    "GitHub 웹훅 생성에 실패했습니다."), ex);
        } catch (RestClientException ex) {
            throw new WebhookRegistrationException("GitHub 웹훅 API 호출 중 오류가 발생했습니다.", ex);
        }
    }

    private void validateConfiguration(String accessToken) {
        if (!StringUtils.hasText(accessToken)) {
            throw new WebhookRegistrationException("GitHub 액세스 토큰이 존재하지 않습니다. 다시 로그인해주세요.");
        }
        if (!StringUtils.hasText(callbackUrl)) {
            throw new WebhookRegistrationException("GitHub 웹훅 콜백 URL이 설정되지 않았습니다.");
        }
        if (!StringUtils.hasText(secret)) {
            throw new WebhookRegistrationException("GitHub 웹훅 시크릿이 설정되지 않았습니다.");
        }
    }

    private List<String> parseEvents(String eventsProperty) {
        if (!StringUtils.hasText(eventsProperty)) {
            return Collections.singletonList("push");
        }
        List<String> parsed = Arrays.stream(eventsProperty.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .collect(Collectors.toList());
        return parsed.isEmpty() ? Collections.singletonList("push") : parsed;
    }

    private String resolveApiError(String responseBody, String fallback) {
        if (!StringUtils.hasText(responseBody)) {
            return fallback;
        }
        try {
            JsonNode node = objectMapper.readTree(responseBody);
            if (node.hasNonNull("message")) {
                return "GitHub 웹훅 생성에 실패했습니다: " + node.get("message").asText();
            }
        } catch (JsonProcessingException ex) {
            log.debug("Failed to parse GitHub error response: {}", responseBody, ex);
        }
        return fallback;
    }

    private record RepositorySlug(String owner, String repository) {
        static RepositorySlug from(String fullName) {
            if (!StringUtils.hasText(fullName) || !fullName.contains("/")) {
                throw new WebhookRegistrationException("owner/repository 형식으로 입력해주세요.");
            }
            String[] parts = fullName.split("/");
            if (parts.length != 2 || !StringUtils.hasText(parts[0]) || !StringUtils.hasText(parts[1])) {
                throw new WebhookRegistrationException("owner/repository 형식으로 입력해주세요.");
            }
            return new RepositorySlug(parts[0].trim(), parts[1].trim());
        }
    }
    
}
