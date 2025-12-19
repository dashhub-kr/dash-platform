package com.ssafy.dash.config;

import com.ssafy.dash.problem.application.ProblemService;
import com.ssafy.dash.problem.application.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class TagInitializer {

    private final TagService tagService;
    private final ProblemService problemService;

    @Bean
    public CommandLineRunner initTags() {
        return args -> {
            tagService.initializeTags();
            problemService.initializeProblems();
        };
    }
}
