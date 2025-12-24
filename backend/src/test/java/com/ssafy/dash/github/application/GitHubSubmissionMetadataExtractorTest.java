package com.ssafy.dash.github.application;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

class GitHubSubmissionMetadataExtractorTest {

    private final GitHubSubmissionMetadataExtractor extractor = new GitHubSubmissionMetadataExtractor();

    @Test
    void extractParsesBaekjoonHubCommitMessage() {
        String message = "[Bronze V] Title: A+B, Time: 108 ms, Memory: 14212 KB -BaekjoonHub";
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

    @Test
    void extractHandlesTitleWithNumbers() {
        String message = "chore: update";
        String path = "백준/Gold/12865.평범한배낭2/Main.java";

        GitHubSubmissionMetadataExtractor.SubmissionMetadata metadata = extractor.extract(message, path);

        assertThat(metadata.platform()).isEqualTo("BAEKJOON");
        assertThat(metadata.problemNumber()).isEqualTo("12865");
        assertThat(metadata.title()).isEqualTo("평범한배낭2");
        assertThat(metadata.language()).isEqualTo("JAVA");
    }

    @Test
    void extractHandles2xnTiling() {
        String message = "solved";
        String path = "백준/Silver/11726.2×n타일링/Main.java";

        GitHubSubmissionMetadataExtractor.SubmissionMetadata metadata = extractor.extract(message, path);

        assertThat(metadata.problemNumber()).isEqualTo("11726");
        assertThat(metadata.title()).isEqualTo("2×n타일링");
    }

    @Test
    void extractParsesSweaCommitMessage() {
        // [D4] Title: [Professional] 키 순서, Time: 257 ms, Memory: 96,036 KB -BaekjoonHub
        String message = "[D4] Title: [Professional] 키 순서, Time: 257 ms, Memory: 96,036 KB -BaekjoonHub";
        String path = "SWEA/D4/1234.키순서/Solution.java";

        GitHubSubmissionMetadataExtractor.SubmissionMetadata metadata = extractor.extract(message, path);

        assertThat(metadata.platform()).isEqualTo("SWEA");
        assertThat(metadata.difficulty()).isEqualTo("D4");
        // Title logic might need check: matcher extracts "[Professional] 키 순서" if not
        // handled carefully,
        // but regex is `Title\s*:\s*(?<title>[^,]+)`, so it should capture
        // `[Professional] 키 순서`.
        assertThat(metadata.title()).isEqualTo("[Professional] 키 순서");
        assertThat(metadata.runtimeMs()).isEqualTo(257);
        assertThat(metadata.memoryKb()).isEqualTo(96036);
    }

    @Test
    void extractParsesSweaProfessionalCommitMessage() {
        // Example: [Professional] Title: ...
        String message = "[Professional] Title: 지하철 노선, Time: 130 ms, Memory: 1,024 KB -BaekjoonHub";
        String path = "some/path/Solution.cpp";

        GitHubSubmissionMetadataExtractor.SubmissionMetadata metadata = extractor.extract(message, path);

        assertThat(metadata.platform()).isEqualTo("SWEA");
        assertThat(metadata.difficulty()).isEqualTo("Professional");
        assertThat(metadata.memoryKb()).isEqualTo(1024);
    }

}
