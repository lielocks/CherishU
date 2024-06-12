package cherish.backend.item.service;

import cherish.backend.item.model.Item;
import cherish.backend.item.repository.ItemRepository;
import cherish.backend.item.repository.LockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class NamedLockViewFacade {

    private final LockRepository lockRepository;
    private final ItemRepository itemRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void increaseViewWithNewPropagation(Long itemId) {
        Item item = itemRepository.findById(itemId).get();
        item.increaseViews();
        itemRepository.saveAndFlush(item);
    }

    @Transactional
    public void increase(Long itemId) {
        int retryCount = 0;
        final int maxRetries = 5;
        final long backoffInterval = 100L; // 백오프 간격 100ms
        boolean acquiredLock = false;

        while (retryCount < maxRetries && !acquiredLock) {
            try {
                acquiredLock = lockRepository.getLock(itemId);
                if (acquiredLock) {
                    increaseViewWithNewPropagation(itemId);
                    break;
                }
            } catch (Exception e) {
                System.out.println("Exception : " + e.getMessage());
                e.printStackTrace();
            } finally {
                if (acquiredLock) {
                    lockRepository.releaseLock(itemId);
                } else {
                    retryCount++;
                    System.out.printf("Lock acquisition failed, retrying %d/%d%n", retryCount, maxRetries);
                    try {
                        Thread.sleep(backoffInterval); // 백오프 간격으로 휴식
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Thread was interrupted during lock acquisition", e);
                    }
                }
            }
        }

    }

}
