package com.ssafy.dash.service.github;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class GitHubApiService {

    private final RestTemplate restTemplate = new RestTemplate();

    public String getUserEmails(String accessToken) {
        String url = "https://api.github.com/user/emails";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setAccept(java.util.List.of(MediaType.APPLICATION_JSON));
        HttpEntity<Void> req = new HttpEntity<>(headers);

        ResponseEntity<String> resp = restTemplate.exchange(url, HttpMethod.GET, req, String.class);

        return resp.getBody();
    }

    public String getUserRepos(String accessToken) {
        String url = "https://api.github.com/user/repos";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setAccept(java.util.List.of(MediaType.APPLICATION_JSON));
        HttpEntity<Void> req = new HttpEntity<>(headers);
        
        ResponseEntity<String> resp = restTemplate.exchange(url, HttpMethod.GET, req, String.class);
        
        return resp.getBody();
    }

    public String getFileContent(String accessToken, String owner, String repo, String path, String ref) {
        String url = String.format("https://api.github.com/repos/%s/%s/contents/%s", owner, repo, path);
        
        if (ref != null && !ref.isBlank()) {
            try {
                url = url + "?ref=" + java.net.URLEncoder.encode(ref, java.nio.charset.StandardCharsets.UTF_8.toString());
            } catch (java.io.UnsupportedEncodingException e) {
                // 무시하고 인코딩되지 않은 ref 사용
                url = url + "?ref=" + ref;
            }
        }
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setAccept(java.util.List.of(MediaType.APPLICATION_JSON));
        HttpEntity<Void> req = new HttpEntity<>(headers);

        ResponseEntity<java.util.Map<String, Object>> resp = restTemplate.exchange(url, HttpMethod.GET, req, new org.springframework.core.ParameterizedTypeReference<java.util.Map<String, Object>>() {});
        if (resp.getBody() == null) return null;
        Object content = resp.getBody().get("content");
        Object encoding = resp.getBody().get("encoding");
        if (content == null) return null;
        String contentStr = content.toString();
        if ("base64".equalsIgnoreCase(String.valueOf(encoding))) {
            byte[] decoded = Base64.getDecoder().decode(contentStr.replaceAll("\n", ""));
            
            return new String(decoded, StandardCharsets.UTF_8);
        }
        
        return contentStr;
    }

    public boolean createWebhook(String accessToken, String owner, String repo, String callbackUrl, String secret) {
        String url = String.format("https://api.github.com/repos/%s/%s/hooks", owner, repo);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        java.util.Map<String, Object> body = new java.util.HashMap<>();
        body.put("name", "web");
        java.util.Map<String, Object> config = new java.util.HashMap<>();
        config.put("url", callbackUrl);
        config.put("content_type", "json");
        if (secret != null) config.put("secret", secret);
        body.put("config", config);
        body.put("events", java.util.List.of("push", "pull_request", "create"));
        body.put("active", true);

        HttpEntity<java.util.Map<String, Object>> req = new HttpEntity<>(body, headers);
        ResponseEntity<String> resp = restTemplate.exchange(url, HttpMethod.POST, req, String.class);
        
        return resp.getStatusCode().is2xxSuccessful();
    }

}
