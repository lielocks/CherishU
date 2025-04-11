package cherish.backend.common.service;

import cherish.backend.common.dto.FcmTokenRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class FcmConsumer {

    private final FirebaseCloudMessageService firebaseService;

    @RabbitListener(queues = "${rabbitmq.queue.name}", containerFactory = "rabbitListenerContainerFactory")
    public void consumeFcmMessage(FcmTokenRequestDto dto) {
        firebaseService.sendMessage(dto.getTargetToken(), dto.getTitle(), dto.getBody());
    }

}