package cherish.backend.item.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ItemSearchCondition {
    private String keyword;
    private Set<String> categoryName; // 1개 이상 가능
    private String jobName;
    private String situationName;
    private String gender;
    private String emotionName;
    private String sort;
}
