package com.ssafy.dash.common.ratelimit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Aspect
@Component
public class RateLimitAspect {

    // 메모리 내에 Bucket 저장 (분산 환경에서는 Redis 등을 사용해야 함)
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Around("@annotation(rateLimit)")
    public Object handleRateLimit(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        String key = generateKey(joinPoint);
        Bucket bucket = buckets.computeIfAbsent(key, k -> createNewBucket(rateLimit));

        if (bucket.tryConsume(1)) {
            return joinPoint.proceed();
        } else {
            log.warn("Rate limit exceeded for key: {}", key);
            throw new RateLimitExceededException();
        }
    }

    private Bucket createNewBucket(RateLimit rateLimit) {
        Refill refill = Refill.greedy(rateLimit.refillTokens(), Duration.ofSeconds(rateLimit.refillDurationSeconds()));
        Bandwidth limit = Bandwidth.classic(rateLimit.capacity(), refill);
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    private String generateKey(ProceedingJoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().toShortString();
        String userKey = resolveUserKey();
        return methodName + ":" + userKey;
    }

    private String resolveUserKey() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            return auth.getName();
        }
        
        // 비로그인 사용자 또는 인증 정보가 없는 경우 IP 주소 사용
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpServletRequest request = attributes.getRequest();
            String xForwardedFor = request.getHeader("X-Forwarded-For");
            if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                return xForwardedFor.split(",")[0].trim();
            }
            return request.getRemoteAddr();
        } catch (Exception e) {
            return "unknown-client";
        }
    }
}
