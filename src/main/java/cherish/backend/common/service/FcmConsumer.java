package cherish.backend.common.service;

import cherish.backend.common.dto.FcmTokenRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Component
@RequiredArgsConstructor
@Slf4j
public class FcmConsumer {

    private final FirebaseCloudMessageService firebaseService;

    private final AtomicInteger counter = new AtomicInteger(0);

    @RabbitListener(queues = "${rabbitmq.queue.name}", containerFactory = "rabbitListenerContainerFactory")
    public void consumeFcmMessage(FcmTokenRequestDto dto) {
        firebaseService.enqueueMessage(dto);
    }

}