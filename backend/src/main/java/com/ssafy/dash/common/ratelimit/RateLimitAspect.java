package com.ssafy.dash.common.ratelimit;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
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

@Slf4j
@Aspect
@Component
public class RateLimitAspect {

    // 메모리 내에 Bucket 저장 (분산 환경에서는 Redis 등을 사용해야 함)
    // 오래 쓰지 않은 버킷은 만료시켜 메모리가 무한히 커지지 않게 한다
    private final Cache<String, Bucket> buckets = Caffeine.newBuilder()
            .maximumSize(100_000)
            .expireAfterAccess(Duration.ofHours(1))
            .build();

    @Around("@annotation(rateLimit)")
    public Object handleRateLimit(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        String key = generateKey(joinPoint);
        Bucket bucket = buckets.get(key, k -> createNewBucket(rateLimit));

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
            return ClientIpResolver.resolve(attributes.getRequest());
        } catch (Exception e) {
            return "unknown-client";
        }
    }
}
