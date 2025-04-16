package cherish.backend.common.controller;

import cherish.backend.common.dto.FcmTokenRequestDto;
import cherish.backend.common.service.FcmMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@RestController
@Slf4j
@RequiredArgsConstructor
public class FcmAlarmController {

    private final FcmMessageService fcmMessageService;

    @PostMapping("/public/fcm/v2")
    public ResponseEntity<Void> sendMessage(@RequestBody FcmTokenRequestDto requestDTO) {
        try {
            fcmMessageService.sendMessageAsync(requestDTO);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("예외 발생 : {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}