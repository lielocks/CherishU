package cherish.backend.item.controller;

import cherish.backend.item.dto.ItemSearchCondition;
import cherish.backend.item.dto.ItemSearchResponseDto;
import cherish.backend.item.service.ItemService;
import cherish.backend.member.model.Member;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PublicItemController.class)
public class
ItemSearchControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private ItemService itemService;

    @Test
    void testSearchItem() throws Exception {
        // given
        ItemSearchCondition condition = new ItemSearchCondition();
        condition.setKeyword("스킨푸드");
        Member member = Member.builder().id(1L).build();

        Pageable pageable = PageRequest.of(0, 10);
        List<ItemSearchResponseDto> expected = Arrays.asList(
                new ItemSearchResponseDto(1008L, "캐롯 카로틴 카밍 워터 패드 (250g, 60매)", "스킨푸드",
                        "스킨푸드의 베스트셀러, 당근패드에요. 자연의 당근씨 오일에서 얻은 베타카로틴 성분이 함유되어 있어 무너진 피부 컨디션을 진정시켜줘요.",
                        26000, "https://firebasestorage.googleapis.com/v0/b/quickstart-1606792103333.appspot.com/o/img%2Fitem%2F01008.png?alt=media", false, 5, LocalDate.now()));
        Page<ItemSearchResponseDto> page = new PageImpl<>(expected, pageable, 20);

        given(itemService.searchItem(condition, member, pageable)).willReturn(page);

        // when
        MvcResult result = mockMvc.perform(get("/public/item/search")
                .param("keyword", condition.getKeyword())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .with(user(String.valueOf(member))))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        // then
        ObjectMapper objectMapper = new ObjectMapper();
        String response = objectMapper.writeValueAsString(page);

        assertEquals(response, result.getResponse().getContentAsString());
    }
}
