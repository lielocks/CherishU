package cherish.backend.item.repository;

import java.util.function.Supplier;

public interface LockRepository<T> {
    void increaseWithNamedLock(final Long lockName, final int timeout, final Supplier<T> supplier);

}
