package cherish.backend.item.repository;

import cherish.backend.item.model.Item;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface ItemRepository extends JpaRepository<Item,Long>, ItemRepositoryCustom, QuerydslPredicateExecutor<Item> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from Item i where i.id = :itemId")
    Item findItemById(@Param("itemId") Long itemId);

    @Modifying
    @Query("update Item i SET i.views = i.views + 1 where i.id = :itemId")
    int updateViews(@Param("itemId") Long itemId);
}
