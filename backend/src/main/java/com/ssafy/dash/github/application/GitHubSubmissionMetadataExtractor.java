package com.ssafy.dash.github.application;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class GitHubSubmissionMetadataExtractor {

    private static final Logger log = LoggerFactory.getLogger(GitHubSubmissionMetadataExtractor.class);

    private static final Pattern COMMIT_MESSAGE_PATTERN = Pattern.compile(
            "\\[(?<difficulty>[^]]+)]\\s*Title\\s*:\\s*(?<title>[^,]+),\\s*Time\\s*:\\s*(?<time>-?[\\d,]+)\\s*ms,\\s*Memory\\s*:\\s*(?<memory>-?[\\d,]+)\\s*KB",
            Pattern.CASE_INSENSITIVE);

    // Pattern for "문제번호.문제이름" format (e.g., "12865.평범한배낭" or "1000.A+B")
    private static final Pattern PROBLEM_NUMBER_TITLE_PATTERN = Pattern.compile("^(\\d+)\\.(.+)$");

    private static final Map<String, String> LANGUAGE_MAP = Map.ofEntries(
            Map.entry("java", "JAVA"),
            Map.entry("kt", "KOTLIN"),
            Map.entry("py", "PYTHON"),
            Map.entry("cpp", "CPP"),
            Map.entry("c", "C"),
            Map.entry("cs", "C#"),
            Map.entry("js", "JAVASCRIPT"),
            Map.entry("ts", "TYPESCRIPT"),
            Map.entry("rb", "RUBY"),
            Map.entry("swift", "SWIFT"),
            Map.entry("go", "GO"),
            Map.entry("rs", "RUST"),
            Map.entry("php", "PHP"),
            Map.entry("scala", "SCALA"),
            Map.entry("dart", "DART"),
            Map.entry("cc", "CPP"));

    public SubmissionMetadata extract(String commitMessage, String filePath) {
        String message = commitMessage == null ? "" : commitMessage.trim();
        log.debug("Extracting metadata for file: {}, Message: [{}]", filePath, message);

        Matcher matcher = COMMIT_MESSAGE_PATTERN.matcher(message);
        boolean matched = matcher.find();

        if (!matched) {
            log.debug("FAIL: Message did not match regex pattern.");
        } else {
            log.debug("SUCCESS: Message matched pattern.");
        }

        String difficulty = matched ? matcher.group("difficulty").trim() : null;
        String titleFromMessage = matched ? matcher.group("title").trim() : null;
        Integer runtime = matched ? parseInteger(matcher.group("time")) : null;
        Integer memory = matched ? parseInteger(matcher.group("memory")) : null;

        String derivedProblemNumber = extractProblemNumber(filePath);
        String derivedTitle = StringUtils.hasText(titleFromMessage) ? titleFromMessage : deriveTitleFromPath(filePath);
        String language = detectLanguage(filePath);
        String platform = detectPlatform(commitMessage, filePath);

        if (!StringUtils.hasText(derivedProblemNumber)) {
            derivedProblemNumber = "UNKNOWN";
        }
        if (!StringUtils.hasText(derivedTitle)) {
            derivedTitle = "Untitled";
        }
        if (!StringUtils.hasText(language)) {
            language = "UNKNOWN";
        }

        return new SubmissionMetadata(
                platform,
                difficulty,
                derivedProblemNumber,
                derivedTitle,
                runtime,
                memory,
                language);
    }

    public LocalDateTime parseCommittedAt(String committedAt) {
        if (!StringUtils.hasText(committedAt)) {
            return null;
        }
        try {
            return LocalDateTime.parse(committedAt);
        } catch (DateTimeParseException ex) {
            return null;
        }
    }

    private Integer parseInteger(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            // Remove commas before parsing
            String cleaned = value.replace(",", "");
            return Integer.parseInt(cleaned);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String detectPlatform(String commitMessage, String filePath) {
        String message = (commitMessage == null ? "" : commitMessage).toLowerCase(Locale.ROOT);
        String path = (filePath == null ? "" : filePath).toLowerCase(Locale.ROOT);

        // 1. Check File Path (Package/Directory Name) - High Priority
        if (path.contains("boj/") || path.contains("baekjoon/")) {
            return "BAEKJOON";
        }
        if (path.contains("swea/") || path.contains("swexpertacademy/")) {
            return "SWEA";
        }

        // 2. Check Commit Message - Medium Priority
        // SWEA tags: [D1], [D2], ..., [Professional]
        if (commitMessage != null
                && (commitMessage.contains("[Professional]") || commitMessage.matches(".*\\[D\\d+].*"))) {
            return "SWEA";
        }

        // 3. Check Commit Message for Keywords - Low Priority
        if (message.contains("baekjoon") || message.contains("백준")) {
            return "BAEKJOON";
        }
        if (message.contains("programmers") || message.contains("프로그래머스")) {
            return "PROGRAMMERS";
        }
        if (message.contains("swea")) {
            return "SWEA";
        }

        return "UNKNOWN";
    }

    private String detectLanguage(String filePath) {
        if (!StringUtils.hasText(filePath)) {
            return "UNKNOWN";
        }
        String fileName = filePath.substring(filePath.lastIndexOf('/') + 1);
        int dot = fileName.lastIndexOf('.');
        if (dot == -1 || dot == fileName.length() - 1) {
            return "UNKNOWN";
        }
        String extension = fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
        return LANGUAGE_MAP.getOrDefault(extension, extension.toUpperCase(Locale.ROOT));
    }

    private String extractProblemNumber(String filePath) {
        if (!StringUtils.hasText(filePath)) {
            return null;
        }
        String[] segments = filePath.split("/");

        // Search from the end for a segment matching "문제번호.문제이름" pattern
        for (int i = segments.length - 1; i >= 0; i--) {
            String segment = segments[i];
            // Strip extension for file names
            if (i == segments.length - 1) {
                segment = stripExtension(segment);
            }

            // Try to match "12865.평범한배낭" pattern
            Matcher matcher = PROBLEM_NUMBER_TITLE_PATTERN.matcher(segment);
            if (matcher.matches()) {
                return matcher.group(1); // Return only the number part
            }
        }

        // Fallback: look for leading digits in segments
        for (int i = segments.length - 1; i >= 0; i--) {
            String segment = segments[i];
            if (i == segments.length - 1) {
                segment = stripExtension(segment);
            }
            String leadingDigits = extractLeadingDigits(segment);
            if (StringUtils.hasText(leadingDigits)) {
                return leadingDigits;
            }
        }

        return stripExtension(segments[segments.length - 1]);
    }

    private String deriveTitleFromPath(String filePath) {
        if (!StringUtils.hasText(filePath)) {
            return null;
        }
        String[] segments = filePath.split("/");
        String fileName = segments[segments.length - 1];
        String base = stripExtension(fileName);
        if (isGenericName(base) && segments.length > 1) {
            base = segments[segments.length - 2];
        }
        int dotIndex = base.indexOf('.');
        if (dotIndex > 0 && Character.isDigit(base.charAt(0))) {
            return base.substring(dotIndex + 1).replace('_', ' ').replace('-', ' ').trim();
        }
        return base;
    }

    private String stripExtension(String fileName) {
        return fileName.contains(".") ? fileName.substring(0, fileName.indexOf('.')) : fileName;
    }

    private String extractLeadingDigits(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        StringBuilder digits = new StringBuilder();
        for (char c : value.toCharArray()) {
            if (Character.isDigit(c)) {
                digits.append(c);
            } else {
                break; // Stop at first non-digit
            }
        }
        return digits.length() > 0 ? digits.toString() : null;
    }

    private boolean isGenericName(String value) {
        if (!StringUtils.hasText(value)) {
            return true;
        }
        String normalized = value.toLowerCase(Locale.ROOT);
        return normalized.startsWith("main") || normalized.startsWith("solution") || normalized.startsWith("source");
    }

    public record SubmissionMetadata(
            String platform,
            String difficulty,
            String problemNumber,
            String title,
            Integer runtimeMs,
            Integer memoryKb,
            String language) {
    }
}
