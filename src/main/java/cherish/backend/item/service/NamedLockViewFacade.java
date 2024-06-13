package cherish.backend.item.service;

import cherish.backend.item.model.Item;
import cherish.backend.item.repository.ItemRepository;
import cherish.backend.item.repository.LockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.atomic.AtomicInteger;

@RequiredArgsConstructor
@Service
public class NamedLockViewFacade {

    private final LockRepository lockRepository;
    private final ItemRepository itemRepository;

    private final AtomicInteger waitingCount = new AtomicInteger(0);

    @Transactional
    public void increase(Long itemId) {
        boolean acquiredLock = false;

        try {
            acquiredLock = lockRepository.getLock(itemId);
            if (acquiredLock) {
                increaseViewsInNewTransaction(itemId, waitingCount.getAndSet(0) + 1);
            } else {
                waitingCount.incrementAndGet();
            }
        } catch (Exception e) {
            System.out.println("Exception : " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (acquiredLock) {
                lockRepository.releaseLock(itemId);
            }
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void increaseViewsInNewTransaction(Long itemId, int additionalViews) {
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new IllegalArgumentException("Item not found"));
        item.increaseViews(additionalViews);
        itemRepository.saveAndFlush(item);
    }

}
