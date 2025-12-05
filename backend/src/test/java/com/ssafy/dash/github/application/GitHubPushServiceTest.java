package com.ssafy.dash.github.application;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.dash.github.domain.GitHubPushEvent;
import com.ssafy.dash.github.domain.GitHubPushEventRepository;

@ExtendWith(MockitoExtension.class)
class GitHubPushServiceTest {

    @Mock
    private GitHubPushEventRepository repository;

    private ObjectMapper objectMapper;

    @InjectMocks
    private GitHubPushService pushService;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        pushService = new GitHubPushService(objectMapper, repository);
    }

    @Test
    void savesOnlyJavaFilesFromCommits() {
        when(repository.findByDeliveryId("delivery-1")).thenReturn(Optional.empty());

        String payload = """
                {
                  \"ref\": \"refs/heads/main\",
                  \"repository\": { \"full_name\": \"utact/dash\" },
                  \"pusher\": { \"name\": \"utact\" },
                  \"head_commit\": {
                    \"id\": \"abc123\",
                    \"message\": \"[Bronze V] Title: A+B, Time: 1 ms, Memory: 1 KB\",
                    \"author\": { \"username\": \"utact\", \"email\": \"utact@test.com\" }
                  },
                  \"commits\": [
                    {
                      \"id\": \"abc123\",
                      \"message\": \"[Bronze V] Title: A+B, Time: 1 ms, Memory: 1 KB\",
                      \"timestamp\": \"2025-12-04T11:00:00Z\",
                      \"added\": [
                        \"백준/Bronze/1000.A+B/A+B.java\",
                        \"백준/Bronze/1000.A+B/README.md\"
                      ],
                      \"modified\": [
                        \"프로그래머스/Level1/hello/hello.java\"
                      ]
                    }
                  ]
                }
                """;

        pushService.handlePushEvent("delivery-1", payload);

        verify(repository).save(argThat(event -> {
            assertThat(event.getRepositoryName()).isEqualTo("utact/dash");
            assertThat(event.getFilesJson()).contains("A+B.java");
            assertThat(event.getFilesJson()).contains("hello.java");
            assertThat(event.getFilesJson()).doesNotContain("README.md");
            return true;
        }));
    }

    @Test
    void ignoresDuplicateDelivery() {
        when(repository.findByDeliveryId("delivery-dup")).thenReturn(Optional.of(new GitHubPushEvent()));

        pushService.handlePushEvent("delivery-dup", "{}");

        verify(repository, never()).save(any());
    }
    
}
