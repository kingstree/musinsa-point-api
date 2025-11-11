package musinsa.points.common.log;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import musinsa.points.common.log.dto.LogType;
import musinsa.points.common.log.dto.RequestLog;
import musinsa.points.common.log.dto.ResponseLog;
import musinsa.points.common.log.util.LogSanitizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.nio.charset.StandardCharsets;

@Component
public class ApiLoggingInterceptor implements HandlerInterceptor {
    private static final Logger REQ_LOG = LoggerFactory.getLogger("api.request");
    private static final Logger RES_LOG = LoggerFactory.getLogger("api.response");
    private static final ObjectMapper M = new ObjectMapper();


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute("__startTs", System.currentTimeMillis());


        RequestLog dto = new RequestLog();
        dto.setLogType(LogType.REQUEST);
        dto.setMethod(request.getMethod());
        dto.setUri(request.getRequestURI() + (request.getQueryString() != null ? ("?" + request.getQueryString()) : ""));


        if (request instanceof ContentCachingRequestWrapper wrapped) {
            byte[] buf = wrapped.getContentAsByteArray();
            if (buf.length > 0) {
                String body = new String(buf, StandardCharsets.UTF_8);
                dto.setBody(LogSanitizer.maskIfJson(body));
            }
        }
        try { REQ_LOG.info(M.writeValueAsString(dto)); } catch (Exception ignore) {}
        return true;
    }


    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)throws Exception {
        ResponseLog dto = new ResponseLog();
        dto.setLogType(LogType.RESPONSE);
        dto.setUri(request.getRequestURI());
        dto.setStatus(response.getStatus());


        if (response instanceof ContentCachingResponseWrapper wrapped) {
            byte[] buf = wrapped.getContentAsByteArray();
            if (buf.length > 0) {
                String body = new String(buf, StandardCharsets.UTF_8);
                String masked = LogSanitizer.maskIfJson(body);
                // ApiResult 응답이면 요약
                try {
                    JsonNode node = M.readTree(masked);
                    if (node.has("success")) {
                        String summary = String.format("{success=%s, message=%s, errorCode=%s}",
                                node.path("success").asText(),
                                node.path("message").asText(""),
                                node.path("errorCode").asText(""));
                        dto.setBody(summary);
                    } else {
                        dto.setBody(masked);
                    }
                } catch (Exception e) {
                    dto.setBody(masked);
                }
            }
        }
        try { RES_LOG.info(M.writeValueAsString(dto)); } catch (Exception ignore) {}
    }
}
