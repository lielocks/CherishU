package cherish.backend.item.controller;

import cherish.backend.auth.security.CurrentUser;
import cherish.backend.common.dto.PageResponse;
import cherish.backend.item.constant.ItemSortConstants;
import cherish.backend.item.dto.*;
import cherish.backend.item.service.ItemService;
import cherish.backend.item.service.RecommendKeywordService;
import cherish.backend.member.model.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/public/item")
public class PublicItemController {

    private final ItemService itemService;
    private final RecommendKeywordService recommendKeywordService;

    @GetMapping("/search")
    public PageResponse<ItemSearchResponseDto> searchItemWithFilter(ItemSearchCondition condition, @CurrentUser Member member, Pageable pageable) {
        if (condition.getSort() != null && !ItemSortConstants.SORT_OPTIONS.contains(condition.getSort())) {
            throw new IllegalArgumentException("지원하지 않는 정렬입니다: " + condition.getSort());
        }
        Page<ItemSearchResponseDto> page = itemService.searchItem(condition, member, pageable);
        return new PageResponse<>(page);
    }

    @GetMapping("/search/v2")
    public PageResponse<SortSearchResponseDto> searchItemOnlyWithSort(SortCondition sortCondition, Pageable pageable) {
        Page<SortSearchResponseDto> page = itemService.searchItemOnlySorting(sortCondition, pageable);
        return new PageResponse<>(page);
    }

    @GetMapping("/{itemId}")
    public ItemInfoViewDto findItemInformation(@PathVariable Long itemId, @CurrentUser Member member) {
        return itemService.findItemInfo(itemId, member);
    }

    @GetMapping("/recommend-keyword")
    public RecommendKeywordResponseDto getKeywords() {
        return recommendKeywordService.getKeywords();
    }
}
