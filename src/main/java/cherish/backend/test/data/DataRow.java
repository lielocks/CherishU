package cherish.backend.test.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
@Data
public class DataRow {
    private long id;
    private String item;
    private String brand;
    private String category;
    private Set<String> categoryChild;
    private String price;
    private String brandUrl;
    private String kakaoUrl;
    private String coupangUrl;
    private String naverUrl;
    private String description;
    private Set<String> job;
    private Set<String> jobChild;
    private String minAge;
    private String maxAge;
    private Set<String> situation;
    private Set<String> emotion;
    private Set<String> gender;
    private Set<String> preference;
    private Set<String> type;
    private Set<String> relation;

    public static DataRow of(String[] row) {
        var idValue = row[0].replaceAll("\uFEFF", ""); // BOM 제거

        return new DataRow(
            Long.parseLong(idValue),
                row[1],
            row[2],
            row[3],
            Arrays.stream(row[4].split(",")).map(String::trim).collect(Collectors.toSet()),
            row[5],
            row[6],
            row[7],
            row[8],
            row[9],
            row[10],
            Arrays.stream(row[11].split(",")).map(String::trim).collect(Collectors.toSet()),
            Arrays.stream(row[12].split(",")).map(String::trim).collect(Collectors.toSet()),
            row[13],
            row[14],
            Arrays.stream(row[15].split(",")).map(String::trim).collect(Collectors.toSet()),
            Arrays.stream(row[16].split(",")).map(String::trim).collect(Collectors.toSet()),
            Arrays.stream(row[17].split(",")).map(String::trim).collect(Collectors.toSet()),
            Arrays.stream(row[18].split(",")).map(String::trim).collect(Collectors.toSet()),
            Arrays.stream(row[19].split(",")).map(String::trim).collect(Collectors.toSet()),
            Arrays.stream(row[20].split(",")).map(String::trim).collect(Collectors.toSet())
        );
    }
}
