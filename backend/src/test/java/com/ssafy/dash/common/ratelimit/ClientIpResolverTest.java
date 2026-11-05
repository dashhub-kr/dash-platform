package com.ssafy.dash.common.ratelimit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class ClientIpResolverTest {

    @Test
    @DisplayName("내부 프록시를 거친 요청은 X-Real-IP를 사용한다")
    void 내부_프록시를_거친_요청은_X_Real_IP를_사용한다() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("172.18.0.5");
        request.addHeader("X-Real-IP", "203.0.113.7");

        assertThat(ClientIpResolver.resolve(request)).isEqualTo("203.0.113.7");
    }

    @Test
    @DisplayName("외부에서 직접 온 요청은 헤더를 무시한다")
    void 외부에서_직접_온_요청은_헤더를_무시한다() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("198.51.100.20");
        request.addHeader("X-Real-IP", "1.2.3.4");
        request.addHeader("X-Forwarded-For", "5.6.7.8");

        assertThat(ClientIpResolver.resolve(request)).isEqualTo("198.51.100.20");
    }

    @Test
    @DisplayName("X-Forwarded-For는 신뢰하지 않는다")
    void X_Forwarded_For는_신뢰하지_않는다() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("172.18.0.5");
        request.addHeader("X-Forwarded-For", "5.6.7.8");

        assertThat(ClientIpResolver.resolve(request)).isEqualTo("172.18.0.5");
    }
}
