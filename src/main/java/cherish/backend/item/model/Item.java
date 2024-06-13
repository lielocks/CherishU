package cherish.backend.item.model;

import cherish.backend.board.model.MonthlyBoardItem;
import cherish.backend.common.model.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Builder
public class Item extends BaseTimeEntity {
    @Id
    private Long id;

    private String brand;
    private String name;
    private String description;
    private int price;
    private int views;
    private String imgUrl;
    private int minAge;
    private int maxAge;

    @OneToMany(mappedBy = "item", fetch= FetchType.LAZY, cascade = CascadeType.ALL)
    private List<ItemFilter> itemFilters = new ArrayList<>();

    @OneToMany(mappedBy = "item", fetch= FetchType.LAZY, cascade = CascadeType.ALL)
    private List<ItemJob> itemJobs = new ArrayList<>();

    @OneToMany(mappedBy = "item", fetch= FetchType.LAZY, cascade = CascadeType.ALL)
    private List<ItemCategory> itemCategories = new ArrayList<>();

    @OneToMany(mappedBy = "item", fetch= FetchType.LAZY, cascade = CascadeType.ALL)
    private List<ItemUrl> itemUrls = new ArrayList<>();

    @OneToMany(mappedBy = "item", fetch= FetchType.LAZY, cascade = CascadeType.ALL)
    private List<ItemLike> itemLikes = new ArrayList<>();

    @OneToMany(mappedBy = "item", fetch= FetchType.LAZY, cascade = CascadeType.ALL)
    private List<MonthlyBoardItem> monthlyBoardItems = new ArrayList<>();

    public void increaseViews() {
        this.views += 1;
    }

    public void increaseViews(int count) {
        this.views += count;
    }
}
