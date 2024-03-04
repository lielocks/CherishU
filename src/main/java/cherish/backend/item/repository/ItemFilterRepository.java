package cherish.backend.item.repository;

import cherish.backend.item.model.ItemFilter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ItemFilterRepository extends JpaRepository<ItemFilter, Long>, ItemFilterRepositoryCustom, QuerydslPredicateExecutor<ItemFilter> {
    @Query("select i from ItemFilter i join i.filter f where i.name = :name and f.id = :filterId and i.item.id = :itemId")
    List<ItemFilter> findItemFilterByNameAndFilterId(@Param("name") String name, @Param("filterId") Long filterId, @Param("itemId") Long itemId);
}
