package cherish.backend.item.model;

import jakarta.persistence.*;
import lombok.*;

@Builder
@ToString
@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class ItemJob {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    private String name;

    // 생성 메소드
    @Enumerated(EnumType.STRING)
    private ItemJob.Step step;

    public enum Step {
        PRIMARY_STEP, SECONDARY_STEP
    }
}
