package musinsa.points.common.log.util;

import musinsa.points.common.log.TraceAndCacheFilter;
import org.slf4j.MDC;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@Component
@ControllerAdvice
public class TraceHeaderResponseAdvice implements ResponseBodyAdvice<Object> {

    private boolean isSwagger(String uri) {
        return uri.startsWith("/api-docs")
                || uri.startsWith("/swagger-ui")
                || "/swagger-ui.html".equals(uri)
                || uri.startsWith("/swagger-resources")
                || uri.startsWith("/webjars")
                || "/favicon.ico".equals(uri);
    }


    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attr != null) {
            String uri = attr.getRequest().getRequestURI();
            if (isSwagger(uri)) return false; // Swagger 응답은 아예 건드리지 않음
        }
        return true;
    }


    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {
        String traceId = MDC.get(TraceAndCacheFilter.TRACE_ID);
        if (traceId != null) response.getHeaders().add("X-Trace-Id", traceId);
        return body;
    }
}
