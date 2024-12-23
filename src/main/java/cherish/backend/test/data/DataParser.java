package cherish.backend.test.data;

import cherish.backend.category.model.Category;
import cherish.backend.category.repository.CategoryRepository;
import cherish.backend.category.repository.FilterRepository;
import cherish.backend.item.constant.ItemUrlPlatforms;
import cherish.backend.item.model.*;
import cherish.backend.item.repository.*;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnResource;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

@ConditionalOnResource(resources = "data.csv")
@Profile("put-data")
@Slf4j
@RequiredArgsConstructor
@Component
public class DataParser {

    private final ItemRepository itemRepository;
    private final CategoryRepository categoryRepository;
    private final ItemCategoryRepository itemCategoryRepository;
    private final ItemUrlRepository itemUrlRepository;
    private final ItemJobRepository itemJobRepository;
    private final FilterRepository filterRepository;
    private final ItemFilterRepository itemFilterRepository;

    @Transactional(rollbackOn = RuntimeException.class)
    public void read() throws RuntimeException {
        try (InputStream inputStream = new ClassPathResource("data.csv").getInputStream();
             CSVReader csvReader = new CSVReader(new InputStreamReader(inputStream))) {

             List<DataRow> dataList = csvReader.readAll().stream()
                    .map(DataRow::of)
                    .toList();

            for (DataRow row : dataList) {
                // item table 저장된것 조회
                int price;
                int minAge;
                int maxAge;
                try {
                    price = Integer.parseInt(row.getPrice());
                } catch (NumberFormatException e) {
                    price = 0;
                }
                try {
                    minAge = Integer.parseInt(row.getMinAge());
                } catch (NumberFormatException e) {
                    minAge = 0;
                }
                try {
                    maxAge = Integer.parseInt(row.getMaxAge());
                } catch (NumberFormatException e) {
                    maxAge = 100;
                }
                Item item = itemRepository.save(
                    Item.builder()
                        .id(row.getId())
                        .name(row.getItem())
                        .brand(row.getBrand())
                        .description(row.getDescription())
                        .price(price)
                        .minAge(minAge)
                        .maxAge(maxAge)
                        .build()
                );
                // category 있는지 조회 후 없으면 저장
                Category category = categoryRepository.findByName(row.getCategory())
                    .orElseGet(() -> categoryRepository.save(
                        Category.builder()
                            .name(row.getCategory())
                            .build()
                    ));
                // 이미 매핑된 item_category의 경우 제외하고 저장
                if (!itemCategoryRepository.existsByItemAndCategory(item, category)) {
                    itemCategoryRepository.save(
                        ItemCategory.builder()
                            .item(item)
                            .category(category)
                            .build()
                    );
                }

                //2차 카테고리 작업
                for (String childCategoryName : row.getCategoryChild()) {
                    Category childCategory = categoryRepository.findByName(childCategoryName)
                        .orElseGet(() -> categoryRepository.save(
                            Category.builder()
                                    .parent(category)
                                .name(childCategoryName)
                                .build()
                        ));
                    if (!itemCategoryRepository.existsByItemAndCategory(item, childCategory)) {
                        itemCategoryRepository.save(
                            ItemCategory.builder()
                                .item(item)
                                .category(childCategory)
                                .build()
                        );
                    }
                }
                // 브랜드 url insert
                if (StringUtils.isNotBlank(row.getBrandUrl()) && !row.getBrandUrl().equals("-")) {
                    itemUrlRepository.findBrandUrlByItem(item)
                        .ifPresentOrElse(itemUrl -> itemUrlRepository.save(
                            ItemUrl.builder()
                                .id(itemUrl.getId())
                                .item(item)
                                .url(row.getBrandUrl())
                                .platform(ItemUrlPlatforms.BRAND)
                                .build()
                        ), () -> itemUrlRepository.save(
                            ItemUrl.builder()
                                .item(item)
                                .url(row.getBrandUrl())
                                .platform(ItemUrlPlatforms.BRAND)
                                .build()
                        ));
                }
                // kakao url
                if (StringUtils.isNotBlank(row.getKakaoUrl()) && !row.getKakaoUrl().equals("-")) {
                    itemUrlRepository.findKakaoUrlByItem(item)
                        .ifPresentOrElse(itemUrl -> itemUrlRepository.save(
                            ItemUrl.builder()
                                .id(itemUrl.getId())
                                .item(item)
                                .url(row.getKakaoUrl())
                                .platform(ItemUrlPlatforms.KAKAO)
                                .build()
                        ), () -> itemUrlRepository.save(
                            ItemUrl.builder()
                                .item(item)
                                .url(row.getKakaoUrl())
                                .platform(ItemUrlPlatforms.KAKAO)
                                .build()
                        ));
                }
                // coupang url
                if (StringUtils.isNotBlank(row.getCoupangUrl()) && !row.getCoupangUrl().equals("-")) {
                    itemUrlRepository.findCoupangUrlByItem(item)
                        .ifPresentOrElse(itemUrl -> itemUrlRepository.save(
                            ItemUrl.builder()
                                .id(itemUrl.getId())
                                .item(item)
                                .url(row.getCoupangUrl())
                                .platform(ItemUrlPlatforms.COUPANG)
                                .build()
                        ), () -> itemUrlRepository.save(
                            ItemUrl.builder()
                                .item(item)
                                .url(row.getCoupangUrl())
                                .platform(ItemUrlPlatforms.COUPANG)
                                .build()
                        ));
                }
                // naver url
                if (StringUtils.isNotBlank(row.getNaverUrl()) && !row.getNaverUrl().equals("-")) {
                    itemUrlRepository.findNaverUrlByItem(item)
                        .ifPresentOrElse(itemUrl -> itemUrlRepository.save(
                            ItemUrl.builder()
                                .id(itemUrl.getId())
                                .item(item)
                                .url(row.getNaverUrl())
                                .platform(ItemUrlPlatforms.NAVER)
                                .build()
                        ), () -> itemUrlRepository.save(
                            ItemUrl.builder()
                                .item(item)
                                .url(row.getNaverUrl())
                                .platform(ItemUrlPlatforms.NAVER)
                                .build()
                        ));
                }
                // 1차 직업군 insert
                for (String jobName : row.getJob()) {
                    if (!itemJobRepository.existsByItemAndName(item, jobName)) {
                        itemJobRepository.save(
                            ItemJob.builder()
                                .item(item)
                                .name(jobName)
                                    .step(ItemJob.Step.PRIMARY_STEP)
                                    .build()
                        );
                    }
                }
                // 2차 직업군 insert
                for (String jobChildName : row.getJobChild()) {
                    if (!itemJobRepository.existsByItemAndName(item, jobChildName)) {
                        itemJobRepository.save(
                            ItemJob.builder()
                                .item(item)
                                    .name(jobChildName)
                                .step(ItemJob.Step.SECONDARY_STEP)
                                .build()
                        );
                    }
                }
                // situation filter id = 2
                long situationId = 2L;
                for (String situation : row.getSituation()) {
                    if (itemFilterRepository.findItemFilterByNameAndFilterId(situation, situationId, item.getId()).isEmpty()) {
                        itemFilterRepository.save(
                            ItemFilter.builder()
                                .item(item)
                                .name(situation)
                                .filter(filterRepository.findById(situationId).get())
                                .build()
                        );
                    }
                }
                long emotionId = 3L;
                for (String emotion : row.getEmotion()) {
                    if (itemFilterRepository.findItemFilterByNameAndFilterId(emotion, emotionId, item.getId()).isEmpty()) {
                        itemFilterRepository.save(
                            ItemFilter.builder()
                                .item(item)
                                .name(emotion)
                                .filter(filterRepository.findById(emotionId).get())
                                .build()
                        );
                    }
                }
                long genderId = 4L;
                for (String gender : row.getGender()) {
                    if (itemFilterRepository.findItemFilterByNameAndFilterId(gender, genderId, item.getId()).isEmpty()) {
                        itemFilterRepository.save(
                            ItemFilter.builder()
                                .item(item)
                                .name(gender)
                                .filter(filterRepository.findById(genderId).get())
                                .build()
                        );
                    }
                }
                long preferenceId = 5L;
                for (String preference : row.getPreference()) {
                    if (itemFilterRepository.findItemFilterByNameAndFilterId(preference, preferenceId, item.getId()).isEmpty()) {
                        itemFilterRepository.save(
                            ItemFilter.builder()
                                .item(item)
                                .name(preference)
                                .filter(filterRepository.findById(preferenceId).get())
                                .build()
                        );
                    }
                }
                long typeId = 6L;
                for (String type : row.getType()) {
                    if (itemFilterRepository.findItemFilterByNameAndFilterId(type, typeId, item.getId()).isEmpty()) {
                        itemFilterRepository.save(
                            ItemFilter.builder()
                                .item(item)
                                .name(type)
                                .filter(filterRepository.findById(typeId).get())
                                .build()
                        );
                    }
                }
                long relationId = 7L;
                for (String relation : row.getRelation()) {
                    if (itemFilterRepository.findItemFilterByNameAndFilterId(relation, relationId, item.getId()).isEmpty()) {
                        itemFilterRepository.save(
                            ItemFilter.builder()
                                .item(item)
                                .name(relation)
                                .filter(filterRepository.findById(relationId).get())
                                .build()
                        );
                    }
                }
            }
        } catch (IOException | CsvException ex) {
            throw new RuntimeException(ex);
        }
    }

    @PostConstruct
    public void test() {
        try {
            long startTimeMillis = System.currentTimeMillis();
            read();
            long endTimeMillis = System.currentTimeMillis();
            log.info("Data Parser V1 Insert time : {}", (endTimeMillis - startTimeMillis));
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}
