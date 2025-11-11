package musinsa.points.presentation;

import com.github.benmanes.caffeine.cache.Cache;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import musinsa.points.presentation.dto.response.LoginResponse;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/_debug/caches")
public class CacheDebugController {

    private final CacheManager cacheManager;

    public CacheDebugController(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }
    @Operation(
            summary = "캐시 상태 조회",
            description = "지정한 캐시 이름(name)에 대한 현재 저장된 항목과 통계 정보를 반환합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "캐시 상태 조회 성공"),
                    @ApiResponse(responseCode = "400", description = "존재하지 않거나 Caffeine 기반이 아닌 캐시")
            }
    )
    @GetMapping("/{name}")
    public ResponseEntity<?> dump(@PathVariable String name) {
        var springCache = cacheManager.getCache(name);
        if (!(springCache instanceof CaffeineCache cache)) {
            return ResponseEntity.badRequest().body("Cache not found or not Caffeine: " + name);
        }
        Cache<Object, Object> nativeCache = cache.getNativeCache();

        // JSON으로 보기 좋게 변환
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("name", name);
        out.put("size", nativeCache.estimatedSize());
        out.put("stats", nativeCache.stats().toString());

        Map<String, Object> entries = new LinkedHashMap<>();
        nativeCache.asMap().forEach((k, v) -> entries.put(String.valueOf(k), v));
        out.put("entries", entries);

        return ResponseEntity.ok(out);
    }
}
