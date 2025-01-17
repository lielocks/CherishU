package cherish.backend.common.config;

import cherish.backend.common.constant.CacheType;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Configuration
@Slf4j
@EnableCaching
public class CacheConfig {

    @Bean
    public List<CaffeineCache> caffeineConfig() {

        return Arrays.stream(CacheType.values())
                .map(cache -> new CaffeineCache(
                        cache.getCacheName(),
                        Caffeine.newBuilder()
                                .recordStats()
                                .expireAfterWrite(cache.getExpireAfterWrite(), TimeUnit.MINUTES)
                                .maximumSize(cache.getMaximumSize())
                                .build()
                ))
                .peek(cache -> log.info("Configured cache :: {} ", cache.getName()))
                .toList();
    }

    @Bean
    public CacheManager cacheManager(List<CaffeineCache> caffeineCaches) {
        final SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(caffeineCaches);
        return cacheManager;
    }
}