package cherish.backend.item.service;

import cherish.backend.item.dto.*;
import cherish.backend.item.model.Item;
import cherish.backend.item.repository.ItemFilterRepository;
import cherish.backend.item.repository.ItemRepository;
import cherish.backend.member.model.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemFilterRepository itemFilterRepository;
    private final ItemRepository itemRepository;

    private ConcurrentHashMap<String, Lock> locks =  new ConcurrentHashMap<>();

    @Transactional(readOnly = true)
    public Page<ItemSearchResponseDto> searchItem(ItemSearchCondition searchCondition, Member member, Pageable pageable) {
        Page<ItemSearchResponseDto> response = itemFilterRepository.searchItem(searchCondition, member, pageable);
        return response;
    }

    @Transactional(readOnly = true)
    public Page<SortSearchResponseDto> searchItemOnlySorting(SortCondition sortCondition, Pageable pageable) {
        return itemFilterRepository.sortItem(sortCondition, pageable);
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

    @Transactional
    public void increaseViewsWithLock(Long itemId) {
        Item item = itemRepository.findItemById(itemId);
        item.increaseViews();
    }

    @Transactional
    public void increaseViewsUpdating(Long itemId) {
        itemRepository.updateViews(itemId);
    }

    @Transactional
    public void increaseViews(Long itemId) {
        Item item = itemRepository.findById(itemId).get();
        item.increaseViews();
    }

    public void increaseViewsRequestLock(Long id) throws InterruptedException {
        Lock lock = locks.computeIfAbsent(String.valueOf(id), key -> new ReentrantLock());
        boolean acquiredLock = lock.tryLock(3, TimeUnit.SECONDS);

        if (!acquiredLock) {
            throw new RuntimeException("Lock acquired FAILED");
        }
        try {
            increaseViews(id);
        } finally {
            lock.unlock();
        }
    }

}
