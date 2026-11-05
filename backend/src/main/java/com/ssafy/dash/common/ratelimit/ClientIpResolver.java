package com.ssafy.dash.common.ratelimit;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * 요청자 IP 판별
 *
 * <p>클라이언트가 임의로 넣을 수 있는 X-Forwarded-For는 쓰지 않는다.
 * 내부 프록시(nginx)를 거친 요청일 때만 프록시가 덮어쓴 X-Real-IP를 신뢰한다.
 */
public final class ClientIpResolver {

    private static final String REAL_IP_HEADER = "X-Real-IP";

    private ClientIpResolver() {
    }

    public static String resolve(HttpServletRequest request) {
        String remoteAddr = request.getRemoteAddr();
        if (isInternal(remoteAddr)) {
            String realIp = request.getHeader(REAL_IP_HEADER);
            if (StringUtils.hasText(realIp)) {
                return realIp.trim();
            }
        }
        return remoteAddr;
    }

    private static boolean isInternal(String address) {
        if (!StringUtils.hasText(address)) {
            return false;
        }
        try {
            InetAddress inet = InetAddress.getByName(address);
            return inet.isLoopbackAddress() || inet.isSiteLocalAddress();
        } catch (UnknownHostException e) {
            return false;
        }
    }

}
