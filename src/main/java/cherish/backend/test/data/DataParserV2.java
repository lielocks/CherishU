package cherish.backend.test.data;

import cherish.backend.item.model.ItemJob;
import com.opencsv.CSVReader;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnResource;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@ConditionalOnResource(resources = "data.csv")
@Profile("put-data")
@Slf4j
@Component
@RequiredArgsConstructor
public class DataParserV2 {

    private final JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void parseAndInsertDataFromCsv() {
        try (InputStream inputStream = new ClassPathResource("data.csv").getInputStream();
             CSVReader csvReader = new CSVReader(new InputStreamReader(inputStream))) {
            long startTimeMillis = System.currentTimeMillis();
            List<DataRow> dataList = csvReader.readAll().stream()
                    .map(DataRow::of)
                    .toList();
            parseAndInsertData(dataList);
            long endTimeMillis = System.currentTimeMillis();
            log.info("Data Parser V2 Insert time : {}", (endTimeMillis - startTimeMillis));
        } catch (IOException e) {
            log.error("CSV File 을 읽는 과정에서 문제 발생", e);
            throw new RuntimeException("CSV File reading, processing 중 Error", e);
        } catch (Exception e) {
            log.error("CSV processing 중 발생한 General Error", e);
            throw new RuntimeException("CSV processing 중 발생한 General Error", e);
        }
    }

    @Transactional
    public void parseAndInsertData(List<DataRow> dataRows) {
        try {
            String insertSql = "INSERT INTO item (id, name, brand, price, description) VALUES (?, ?, ?, ?, ?)";
            String categorySql = "INSERT INTO category (name) VALUES (?) " + "ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name";
            String itemCategorySql = "INSERT INTO category (name) VALUES (?)";
            String itemUrlSql = "INSERT INTO item_url (item_id, url) VALUES (?, ?)";
            String itemJobSql = "INSERT INTO item_job (item_id, name, step) VALUES (?, ?, ?)";
            String filterSql = "INSERT INTO item_filter (item_id, filter_id, name) VALUES (?, ?, ?)";

            List<Object[]> itemBatchArgs;
            List<Object[]> categoryBatchArgs;
            List<Object[]> itemCategoryBatchArgs;
            List<Object[]> itemUrlBatchArgs;
            List<Object[]> itemJobBatchArgs = new ArrayList<>();
            List<Object[]> filterBatchArgs = new ArrayList<>();

            long situationId = 2L;
            long emotionId = 3L;
            long genderId = 4L;
            long preferenceId = 5L;
            long typeId = 6L;
            long relationId = 7L;

            itemBatchArgs = dataRows.stream()
                    .map(row -> {
                        int price = 0;
                        if (row.getPrice() != null && !row.getPrice().isEmpty()) {
                            try {
                                price = Integer.parseInt(row.getPrice());
                            } catch (NumberFormatException e) {
                                log.warn("해당 item 에 올바른 price 가 설정되어 있지 않습니다. {}: {}", row.getId(), row.getPrice());
                            }
                        }
                        return new Object[]{
                                row.getId(),
                                row.getItem(),
                                row.getBrand(),
                                price,
                                row.getDescription()
                        };
                    })
                    .toList();

            categoryBatchArgs = dataRows.stream()
                    .map(row -> new Object[]{
                            row.getCategory()
                    })
                    .distinct()
                    .toList();

            itemCategoryBatchArgs = dataRows.stream()
                    .flatMap(row -> row.getCategoryChild().stream()
                            .map(child -> new Object[]{child}))
                    .toList();

            itemUrlBatchArgs = dataRows.stream()
                    .map(row -> {
                        return List.of(
                                new Object[]{row.getId(), row.getBrandUrl()},
                                new Object[]{row.getId(), row.getKakaoUrl()},
                                new Object[]{row.getId(), row.getCoupangUrl()},
                                new Object[]{row.getId(), row.getNaverUrl()}
                        );
                    })
                    .flatMap(List::stream)
                    .filter(args -> args[1] != null && !((String) args[1]).isBlank())
                    .toList();

            for (DataRow row : dataRows) {
                for (String jobName : row.getJob()) {
                    itemJobBatchArgs.add(new Object[]{row.getId(), jobName, ItemJob.Step.PRIMARY_STEP.name()});  // Convert enum to string
                }

                for (String jobChildName : row.getJobChild()) {
                    itemJobBatchArgs.add(new Object[]{row.getId(), jobChildName, ItemJob.Step.SECONDARY_STEP.name()});  // Convert enum to string
                }
            }

            for (DataRow row : dataRows) {
                row.getSituation().forEach(filter ->
                        filterBatchArgs.add(new Object[]{row.getId(), situationId, filter}));
                row.getEmotion().forEach(filter ->
                        filterBatchArgs.add(new Object[]{row.getId(), emotionId, filter}));
                row.getGender().forEach(filter ->
                        filterBatchArgs.add(new Object[]{row.getId(), genderId, filter}));
                row.getPreference().forEach(filter ->
                        filterBatchArgs.add(new Object[]{row.getId(), preferenceId, filter}));
                row.getType().forEach(filter ->
                        filterBatchArgs.add(new Object[]{row.getId(), typeId, filter}));
                row.getRelation().forEach(filter ->
                        filterBatchArgs.add(new Object[]{row.getId(), relationId, filter}));
            }

            jdbcTemplate.batchUpdate(insertSql, itemBatchArgs);
            jdbcTemplate.batchUpdate(categorySql, categoryBatchArgs);
            jdbcTemplate.batchUpdate(itemCategorySql, itemCategoryBatchArgs);
            jdbcTemplate.batchUpdate(itemUrlSql, itemUrlBatchArgs);
            jdbcTemplate.batchUpdate(itemJobSql, itemJobBatchArgs);
            jdbcTemplate.batchUpdate(filterSql, filterBatchArgs);

        } catch (Exception e) {
            log.error("CSV parsing 후 DB insert 하는 과정에서 Error", e);
            throw new RuntimeException("CSV parsing 후 DB insert 하는 과정에서 Error", e);
        }
    }
}
