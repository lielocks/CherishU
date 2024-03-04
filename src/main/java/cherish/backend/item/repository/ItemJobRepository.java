package cherish.backend.item.repository;

import cherish.backend.item.model.Item;
import cherish.backend.item.model.ItemJob;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ItemJobRepository extends JpaRepository<ItemJob, Long> {
    boolean existsByItemAndName(Item item, String itemJobName);
    Optional<ItemJob> findByName(String jobName);
}
