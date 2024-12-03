package cherish.backend.item.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@Builder
public class SortSearchResponseDto {
    private Long id;
    private String name;
    private String brand;
    private String description;
    private int price;
    private String imgUrl;
    @JsonIgnore
    private int views;
    @JsonIgnore
    private LocalDate modifiedDate;

    @QueryProjection
    public SortSearchResponseDto(Long id, String name, String brand, String description, int price, String imgUrl, int views, LocalDate modifiedDate) {
        this.id = id;
        this.name = name;
        this.brand = brand;
        this.description = description;
        this.price = price;
        this.imgUrl = imgUrl;
        this.views = views;
        this.modifiedDate = modifiedDate;
    }
}
