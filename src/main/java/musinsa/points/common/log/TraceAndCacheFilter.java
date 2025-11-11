package musinsa.points.common.log;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.util.UUID;

public class TraceAndCacheFilter extends OncePerRequestFilter {

    public static final String TRACE_ID = "traceId";


    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String u = request.getRequestURI();

        boolean isAccepted =  u.startsWith("/api-docs")
                || u.startsWith("/swagger-ui")
                || u.equals("/swagger-ui.html")
                || u.startsWith("/swagger-resources")
                || u.startsWith("/webjars")
                || u.equals("/favicon.ico")
                || u.equals("/health")
                || u.startsWith("/actuator")
                || u.startsWith("/error")
                || u.startsWith("/v3/api-docs")
                || u.startsWith("/h2-console/");
        if(!isAccepted) {
            logger.info(request.getRequestURI() + " is not accepted for traceId");
        }

        return isAccepted;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {


        // traceId: 클라이언트 제공값(X-Trace-Id) 우선, 없으면 생성
        String traceId = request.getHeader("X-Trace-Id");
        if (traceId == null || traceId.isBlank()) traceId = UUID.randomUUID().toString();
        MDC.put(TRACE_ID, traceId);

        try {
            if (shouldNotFilter(request)) {
                chain.doFilter(request, response);
                return;
            }

            // 일반 경로만 캐싱 래퍼 적용 (인터셉터가 바디 읽을 수 있게 함)
            ContentCachingRequestWrapper req = new ContentCachingRequestWrapper(request);
            ContentCachingResponseWrapper res = new ContentCachingResponseWrapper(response);
            try {
                chain.doFilter(req, res);
            } finally {
                // 반드시 마지막에 한 번만
                res.copyBodyToResponse();
            }
        } finally {
            MDC.remove(TRACE_ID);
        }
    }
}
