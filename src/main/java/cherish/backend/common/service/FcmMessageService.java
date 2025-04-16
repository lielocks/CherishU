package cherish.backend.common.service;

import cherish.backend.common.dto.FcmTokenRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;


@Service
@RequiredArgsConstructor
@Slf4j
public class FcmMessageService {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.name}")
    private String exchange;

    @Value("${rabbitmq.routing.key}")
    private String routingKey;

    @Async("alarmExecutor")
    public void sendMessageAsync(FcmTokenRequestDto requestDTO) {
        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, requestDTO, msg -> {
                msg.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                return msg;
            });
        } catch (Exception e) {
            log.error("Message 전송 FAILED: {}", e.getMessage(), e);
        }
    }
}

