package cherish.backend.item.service;

import cherish.backend.item.dto.ItemInfoResponseDto;
import cherish.backend.item.dto.ItemInfoViewDto;
import cherish.backend.item.dto.ItemSearchCondition;
import cherish.backend.item.dto.ItemSearchResponseDto;
import cherish.backend.item.model.Item;
import cherish.backend.item.repository.ItemFilterRepository;
import cherish.backend.item.repository.ItemRepository;
import cherish.backend.member.model.Member;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional
@RequiredArgsConstructor
public class ItemService {
    @PersistenceContext
    private EntityManager em;
    private final ItemFilterRepository itemFilterRepository;
    private final ItemRepository itemRepository;

    @Transactional(readOnly = true)
    public Page<ItemSearchResponseDto> searchItem(ItemSearchCondition searchCondition, Member member, Pageable pageable) {
        Page<ItemSearchResponseDto> response = itemFilterRepository.searchItem(searchCondition, member, pageable);
        return response;
    }

    public ItemInfoViewDto findItemInfo(Long itemId, Member member) {
        increaseViewsUpdating(itemId);
        List<ItemInfoResponseDto> itemResponses = itemRepository.itemResponse(itemId, member);
        ItemInfoResponseDto itemInfoResponseDto = itemResponses.get(0);

        Set<String> platforms = new LinkedHashSet<>();
        Set<String> urls = new LinkedHashSet<>();

        for (ItemInfoResponseDto itemResponse : itemResponses) {
            if (itemResponse.getPlatform() != null && itemResponse.getUrl() != null) {
                platforms.add(itemResponse.getPlatform());
                urls.add(itemResponse.getUrl());
            }
        }

        itemInfoResponseDto.setUrl(String.join(", ", urls));
        itemInfoResponseDto.setPlatform(String.join(", ", platforms));

        List<String> filterTags = itemResponses.stream()
                .map(ItemInfoResponseDto::getFilterTag)
                .distinct()
                .limit(2)
                .toList();

        itemInfoResponseDto.setFilterTag(filterTags.toString());
        ItemInfoViewDto itemInfoViewDto = new ItemInfoViewDto(itemInfoResponseDto);

        return itemInfoViewDto;
    }

    public void increaseViewsWithLock(Long itemId) {
        Item item = itemRepository.findItemById(itemId);
        item.increaseViews();
    }

    public void increaseViewsUpdating(Long itemId) {
        itemRepository.updateViews(itemId);
    }
}
