package cherish.backend.item.repository;

import cherish.backend.item.model.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

@Slf4j
@RequiredArgsConstructor
@Repository
public class NamedLockRepository implements LockRepository {

    private final ItemRepository itemRepository;

    private static final String GET_LOCK = "SELECT pg_try_advisory_lock(?)";
    private static final String RELEASE_LOCK = "SELECT pg_advisory_unlock(?)";

    private final AtomicInteger waitingCount = new AtomicInteger(0);

    private final JdbcTemplate jdbcTemplate;


    @Transactional
    public void increaseViews(Long itemId) {
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new IllegalArgumentException("Item not found"));
        increaseWithNamedLock(
                itemId,
                5000,
                () -> {
                    item.increaseViews();
                    return null;
                }
        );
    }

    @Transactional
    public void increaseCountViews(Long itemId, int count) {
        Item item = itemRepository.findById(itemId).get();
        item.increaseViews(count);
    }

    @Override
    @Transactional(propagation = REQUIRES_NEW, timeout = 1)
    public void increaseWithNamedLock(Long lockName, int timeout, Supplier supplier) {
        try {
            boolean lockAcquired = getLock(lockName);
            log.info("Start getLock={}, timeout Seconds={}", lockName, timeout);
            if (!lockAcquired) {
                waitingCount.getAndIncrement();
                log.warn("Failed lockName={}", lockName);
                return;
            }
            log.info("Success getLock={}, timeout Seconds={}", lockName, timeout);
            supplier.get();
        } finally {
            releaseLock(lockName);
            if (waitingCount.get() > 0) {
                log.info("Increasing views for lockName={} after waiting", lockName);
                increaseCountViews(1L, waitingCount.get());
                waitingCount.set(0);
            }
        }
    }

    private boolean getLock(Long lockName) {
        Boolean result = jdbcTemplate.queryForObject(GET_LOCK, Boolean.class, lockName);
        return result != null && result;
    }

    private void releaseLock(Long lockName) {
        Boolean result = jdbcTemplate.queryForObject(RELEASE_LOCK, Boolean.class, lockName);
        if (result == null || !result) {
            log.error("Failed to release lock ={}", lockName);
        } else {
            log.info("Successfully released lock ={}", lockName);
        }
    }
}
