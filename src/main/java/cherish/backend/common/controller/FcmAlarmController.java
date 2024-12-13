package cherish.backend.common.controller;

import cherish.backend.common.dto.FcmTokenRequestDto;
import cherish.backend.common.service.FirebaseCloudMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@Slf4j
@RequiredArgsConstructor
public class FcmAlarmController {

    private final FirebaseCloudMessageService firebaseCloudMessageService;

    @PostMapping("/public/fcm")
    public ResponseEntity pushMessage(@RequestBody FcmTokenRequestDto requestDTO) throws IOException {
        firebaseCloudMessageService.sendMessageTo(
                requestDTO.getTargetToken(),
                requestDTO.getTitle(),
                requestDTO.getBody());

        return ResponseEntity.ok().build();
    }
}