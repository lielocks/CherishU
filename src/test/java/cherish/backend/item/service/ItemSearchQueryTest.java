package cherish.backend.item.service;

import cherish.backend.common.config.QueryDslConfig;
import cherish.backend.item.dto.ItemSearchCondition;
import cherish.backend.item.dto.ItemSearchResponseDto;
import cherish.backend.item.repository.ItemFilterRepository;
import cherish.backend.item.repository.ItemRepository;
import cherish.backend.member.model.Member;
import cherish.backend.member.repository.MemberRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@Transactional
@ActiveProfiles("test")
@Import(QueryDslConfig.class)
public class ItemSearchQueryTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    ItemRepository itemRepository;

    @Autowired
    ItemFilterRepository itemFilterRepository;

    @MockBean
    ItemService itemService;

    @DisplayName("ItemSearchCondition 에 keyword 만 들어올때 default join 문만 타는지 확인 ")
    @Test
    public void itemSearchConditionOnlyWithKeyword() {
        Pageable pageable = PageRequest.of(0, 10);

        Member defaultMember =
                memberRepository.findById(1L).get();
        System.out.println("defaultMember" + defaultMember);
        ItemSearchCondition onlyKeywordCondition = ItemSearchCondition.builder().keyword("탬버린즈").build();

        long before = System.currentTimeMillis();
        Page<ItemSearchResponseDto> responseData =
                itemService.searchItem(onlyKeywordCondition, defaultMember, pageable);
        System.out.println("responseData " + responseData.getContent().get(0));
        long after = System.currentTimeMillis();

        Assertions.assertThat(responseData.getContent().get(0).getBrand().equals("탬버린즈"));
        System.out.println("item search condition only with keyword " + (after - before));
    }


}
