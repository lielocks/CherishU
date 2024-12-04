package cherish.backend.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CacheType {
    SORT_PAGE_CACHE("sortPageCache", 30, 500); // 만료 시간(30분)

    private final String cacheName;
    private final int expireAfterWrite;
    private final int maximumSize;
}
