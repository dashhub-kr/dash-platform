package com.ssafy.dash.common.ratelimit;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
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

/**
 * Global DoS Protection Filter
 * 모든 요청에 대해 IP 기반의 Rate Limit를 적용하여 DoS 공격을 방어합니다.
 * AOP로는 커버되지 않는 Filter Chain 단계(Spring Security 등)의 요청도 방어할 수 있습니다.
 */
@Slf4j
@Component
public class GlobalRateLimitFilter extends OncePerRequestFilter {

    // IP당 분당 100회 요청 허용 (일반적인 API 사용량 고려)
    private static final int CAPACITY = 100;
    private static final int REFILL_TOKENS = 100;
    private static final int REFILL_DURATION_SECONDS = 60;
    private static final long MAX_TRACKED_CLIENTS = 100_000;

    // 오래 쓰지 않은 버킷은 만료시켜 IP가 늘어나도 메모리가 무한히 커지지 않게 한다
    private final Cache<String, Bucket> buckets = Caffeine.newBuilder()
            .maximumSize(MAX_TRACKED_CLIENTS)
            .expireAfterAccess(Duration.ofMinutes(10))
            .build();

    @Override
    protected void doFilterInternal(@org.springframework.lang.NonNull HttpServletRequest request,
            @org.springframework.lang.NonNull HttpServletResponse response,
            @org.springframework.lang.NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String clientIp = ClientIpResolver.resolve(request);
        Bucket bucket = buckets.get(clientIp, this::createNewBucket);

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
}
