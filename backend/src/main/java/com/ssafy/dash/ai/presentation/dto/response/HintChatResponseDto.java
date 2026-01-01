package com.ssafy.dash.ai.presentation.dto.response;

import java.util.List;

import com.ssafy.dash.ai.infrastructure.client.dto.response.HintChatResponse;

public record HintChatResponseDto(
        String reply,
        String teachingStyle,
        List<String> followUpQuestions,
        List<String> relatedConcepts,
        String encouragement) {

    public static HintChatResponseDto from(HintChatResponse response) {
        return new HintChatResponseDto(
                response.getReply(),
                response.getTeachingStyle(),
                response.getFollowUpQuestions(),
                response.getRelatedConcepts(),
                response.getEncouragement());
    }
}
