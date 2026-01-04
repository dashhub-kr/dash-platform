package com.ssafy.dash.common.ratelimit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Global DoS Protection Filter
 * 모든 요청에 대해 IP 기반의 Rate Limit를 적용하여 DoS 공격을 방어합니다.
 * AOP로는 커버되지 않는 Filter Chain 단계(Spring Security 등)의 요청도 방어할 수 있습니다.
 */
@Slf4j
@Component
public class GlobalRateLimitFilter extends OncePerRequestFilter {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    // IP당 분당 100회 요청 허용 (일반적인 API 사용량 고려)
    private static final int CAPACITY = 100;
    private static final int REFILL_TOKENS = 100;
    private static final int REFILL_DURATION_SECONDS = 60;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String clientIp = resolveClientIp(request);
        Bucket bucket = buckets.computeIfAbsent(clientIp, this::createNewBucket);

        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            log.warn("Global rate limit exceeded for IP: {}", clientIp);
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.getWriter().write("Too many requests from your IP. Please try again later.");
        }
    }

    private Bucket createNewBucket(String key) {
        Refill refill = Refill.greedy(REFILL_TOKENS, Duration.ofSeconds(REFILL_DURATION_SECONDS));
        Bandwidth limit = Bandwidth.classic(CAPACITY, refill);
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    private String resolveClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
