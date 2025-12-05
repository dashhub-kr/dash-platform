package com.ssafy.dash.github.application;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class GitHubSubmissionMetadataExtractor {

    private static final Pattern COMMIT_MESSAGE_PATTERN = Pattern.compile(
            "\\[(?<difficulty>[^]]+)]\\s*Title:\\s*(?<title>[^,]+),\\s*Time:\\s*(?<time>\\d+)\\s*ms,\\s*Memory:\\s*(?<memory>\\d+)\\s*KB",
            Pattern.CASE_INSENSITIVE);

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
            Map.entry("dart", "DART")
    );

    public SubmissionMetadata extract(String commitMessage, String filePath) {
        String message = commitMessage == null ? "" : commitMessage.trim();
        Matcher matcher = COMMIT_MESSAGE_PATTERN.matcher(message);
        boolean matched = matcher.find();

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
                language
            );
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
        try {
            return value == null ? null : Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String detectPlatform(String commitMessage, String filePath) {
        String source = ((commitMessage == null ? "" : commitMessage) + " " + (filePath == null ? "" : filePath))
                .toLowerCase(Locale.ROOT);
        if (source.contains("baekjoon") || source.contains("백준")) {
            return "BAEKJOON";
        }
        if (source.contains("programmers") || source.contains("프로그래머스")) {
            return "PROGRAMMERS";
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
        String fileName = segments[segments.length - 1];
        String base = stripExtension(fileName);
        String digits = digitsFrom(base);
        if (StringUtils.hasText(digits)) {
            return digits;
        }
        for (int i = segments.length - 2; i >= 0; i--) {
            digits = digitsFrom(segments[i]);
            if (StringUtils.hasText(digits)) {
                return digits;
            }
        }
        return base;
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

    private String digitsFrom(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String digits = value.replaceAll("[^0-9]", "");
        return StringUtils.hasText(digits) ? digits : null;
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
            String language
    ) {
    }
}
