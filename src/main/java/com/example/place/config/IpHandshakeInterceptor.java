package com.example.place.config;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

public class IpHandshakeInterceptor extends HttpSessionHandshakeInterceptor {

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {

        if (request instanceof ServletServerHttpRequest) {
            ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
            HttpServletRequest req = servletRequest.getServletRequest();

            // 1. ngrok 및 프록시 서버가 유저 진짜 IP를 담아 보내는 헤더 확인
            String ip = req.getHeader("X-Forwarded-For");

            // 2. 헤더가 없거나 비어있다면 기본 IP 추출 방법 사용
            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = req.getRemoteAddr();
            }
            // 3. ngrok을 거쳐오면서 IP가 여러 개 콤마로 묶여 들어올 경우, 첫 번째가 진짜 유저 IP
            else if (ip.contains(",")) {
                ip = ip.split(",")[0].trim();
            }

            // 4. ChatController에서 꺼내 쓸 수 있도록 "ip"라는 키값으로 세션에 저장!
            attributes.put("ip", ip);
        }

        return super.beforeHandshake(request, response, wsHandler, attributes);
    }
}