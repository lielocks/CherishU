package cherish.backend.item.dto;

import cherish.backend.item.constant.ItemSortConstants;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SortCondition {
    private String sort;

    @Override
    public String toString() {
        return sort != null ? sort : ItemSortConstants.MOST_RECOMMENDED;
    }
}
