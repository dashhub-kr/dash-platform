package com.ssafy.dash.common.ratelimit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * API 요청 제한(Rate Limiting)을 적용하기 위한 어노테이션.
 * 메서드 레벨에 적용하며, 사용자(로그인 시) 또는 IP(비로그인 시) 기준으로 제한합니다.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {

    /**
     * 버킷의 최대 용량 (허용 가능한 최대 토큰 수)
     */
    int capacity();

    /**
     * 리필 주기마다 충전되는 토큰 수
     */
    int refillTokens();

    /**
     * 리필 주기 (초 단위)
     */
    int refillDurationSeconds();
}
