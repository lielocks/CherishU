package cherish.backend.item.repository;

import cherish.backend.item.model.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LockRepository extends JpaRepository<Item, Long> {
    @Query(value = "SELECT pg_try_advisory_lock(:key)", nativeQuery = true)
    Boolean getLock(@Param("key") Long key);

    @Query(value = "SELECT pg_advisory_unlock(:key)", nativeQuery = true)
    Boolean releaseLock(@Param("key") Long key);
}
