package cherish.backend.item.service;

import cherish.backend.item.repository.LockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class NamedLockViewFacade {

    private final LockRepository lockRepository;
    private final ItemService itemService;

    @Transactional
    public void increase(Long itemId) {
        int retryCount = 0;
        final int maxRetries = 5;
        final long backoffInterval = 1000L; // 1 second
        boolean acquiredLock = false;

        while (retryCount < maxRetries && !acquiredLock) {

            try {
                acquiredLock = lockRepository.getLock(itemId);
                System.out.println("acquired lock : " + acquiredLock + " for item: " + itemId);
                itemService.increaseViewWithNewPropagation(itemId);
            }

            catch (Exception e) {
                System.out.println("Exception during lock acquisition or view increase: " + e.getMessage());
                e.printStackTrace();
            }

            finally {
                if (acquiredLock) {
                    lockRepository.releaseLock(itemId);
                } else {
                    retryCount++;
                    try {
                        Thread.sleep(backoffInterval * retryCount); // Exponential backoff
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Thread was interrupted during lock acquisition", e);
                    }
                }
            }

        }

        if (!acquiredLock) {
            throw new RuntimeException("Lock acquisition failed after retries: [id: %d]".formatted(itemId));
        }
    }
}
