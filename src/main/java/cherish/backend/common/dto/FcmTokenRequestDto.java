package cherish.backend.common.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FcmTokenRequestDto {
    private String targetToken;
    private String title;
    private String body;
}