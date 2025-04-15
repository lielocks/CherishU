package cherish.backend.common.controller;

import cherish.backend.common.dto.FcmTokenRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@RestController
@Slf4j
@RequiredArgsConstructor
public class FcmAlarmController {

//    private final FirebaseCloudMessageService firebaseCloudMessageService;

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.name}")
    private String exchange;

    @Value("${rabbitmq.routing.key}")
    private String routingKey;

//    @PostMapping("/public/fcm")
//    public ResponseEntity pushMessage(@RequestBody FcmTokenRequestDto requestDTO) throws IOException {
//        firebaseCloudMessageService.sendMessageTo(
//                requestDTO.getTargetToken(),
//                requestDTO.getTitle(),
//                requestDTO.getBody());
//
//        return ResponseEntity.ok().build();
//    }

    @PostMapping("/public/fcm/v2")
    public ResponseEntity sendMessage(@RequestBody FcmTokenRequestDto requestDTO) {
        rabbitTemplate.convertAndSend(exchange, routingKey, requestDTO);
        return ResponseEntity.ok().build();
    }

}