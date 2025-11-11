package musinsa.points.common.log.config;


import musinsa.points.common.log.ApiLoggingInterceptor;
import musinsa.points.common.log.TraceAndCacheFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcLoggingConfig implements WebMvcConfigurer {

    private final ApiLoggingInterceptor interceptor;

    public WebMvcLoggingConfig(ApiLoggingInterceptor interceptor) {
        this.interceptor = interceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(interceptor)
                .addPathPatterns("/**")
                // 필터와 동일한 경로는 인터셉터도 제외
                .excludePathPatterns(
                        "/", "/api-docs/**", "/api-docs",
                        "/v3/api-docs/**", "/v3/api-docs",
                        "/swagger-ui/**", "/swagger-ui.html",
                        "/swagger-resources/**",
                        "/webjars/**",
                        "/favicon.ico",
                        "/health",
                        "/actuator/**",
                        "/error/**", "/error"
                );
    }

    @Bean
    public FilterRegistrationBean<TraceAndCacheFilter> traceAndCacheFilter() {
        FilterRegistrationBean<TraceAndCacheFilter> reg = new FilterRegistrationBean<>();
        reg.setFilter(new TraceAndCacheFilter());
        reg.addUrlPatterns("/*");
        reg.setOrder(1); // 보통 보안/트레이싱 필터 뒤, Spring MVC 전 단계
        return reg;
    }
}
