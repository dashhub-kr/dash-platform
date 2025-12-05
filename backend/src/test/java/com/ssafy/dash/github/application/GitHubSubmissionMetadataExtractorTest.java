package com.ssafy.dash.github.application;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

class GitHubSubmissionMetadataExtractorTest {

    private final GitHubSubmissionMetadataExtractor extractor = new GitHubSubmissionMetadataExtractor();

    @Test
    void extractParsesBaekjoonHubCommitMessage() {
        String message = "[Bronze V] Title: A+B, Time: 1 ms, Memory: 1 KB -BaekjoonHub";
        String path = "백준/Bronze/1000.A+B.java";

        GitHubSubmissionMetadataExtractor.SubmissionMetadata metadata = extractor.extract(message, path);

        assertThat(metadata.platform()).isEqualTo("BAEKJOON");
        assertThat(metadata.difficulty()).isEqualTo("Bronze V");
        assertThat(metadata.problemNumber()).isEqualTo("1000");
        assertThat(metadata.title()).isEqualTo("A+B");
        assertThat(metadata.runtimeMs()).isEqualTo(108);
        assertThat(metadata.memoryKb()).isEqualTo(14212);
        assertThat(metadata.language()).isEqualTo("JAVA");
    }

    @Test
    void extractFallsBackWhenCommitMessageMissing() {
        String message = "chore: update solution";
        String path = "백준/Gold/12345.SomeProblem/Main.kt";

        GitHubSubmissionMetadataExtractor.SubmissionMetadata metadata = extractor.extract(message, path);

        assertThat(metadata.platform()).isEqualTo("BAEKJOON");
        assertThat(metadata.difficulty()).isNull();
        assertThat(metadata.problemNumber()).isEqualTo("12345");
        assertThat(metadata.title()).contains("SomeProblem");
        assertThat(metadata.language()).isEqualTo("KOTLIN");
    }
    
}
