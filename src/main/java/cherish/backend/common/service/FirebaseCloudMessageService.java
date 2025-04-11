package cherish.backend.common.service;

import cherish.backend.common.dto.FcmTokenRequestDto;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;


@Component
@Slf4j
@RequiredArgsConstructor
public class FirebaseCloudMessageService {

    private final BlockingQueue<FcmTokenRequestDto> queue = new LinkedBlockingQueue<>(20000); // 큐 최대 사이즈
    private final ExecutorService executorService = Executors.newFixedThreadPool(5); // 스레드 수 제한

    @PostConstruct
    public void init() {
        for (int i = 0; i < 5; i++) {
            executorService.submit(this::processQueue);
        }
    }

    public void enqueueMessage(FcmTokenRequestDto dto) {
        try {
            queue.put(dto); // 큐가 꽉 차면 블로킹됨 → 유실 없음
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Queue put interrupted: {}", e.getMessage(), e);
        }
    }

    private void processQueue() {
        while (true) {
            try {
                FcmTokenRequestDto dto = queue.take();
                sendMessage(dto);
                Thread.sleep(5);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void sendMessage(FcmTokenRequestDto dto) {
        try {
            Message message = Message.builder()
                    .setToken(dto.getTargetToken())
                    .setNotification(Notification.builder()
                            .setTitle(dto.getTitle())
                            .setBody(dto.getBody())
                            .build())
                    .build();

            String response = FirebaseMessaging.getInstance().send(message);
            log.info("Successfully sent to {}: {}", dto.getTargetToken(), response);
        } catch (Exception e) {
            log.error("Error sending message to {}: {}", dto.getTargetToken(), e.getMessage(), e);
        }
    }

    @PreDestroy
    public void shutdown() {
        executorService.shutdownNow();
    }

    @Async("alarmExecutor")
    public void sendMessageTo(String targetToken, String title, String body) {
        try {
            Message message = Message.builder()
                    .setToken(targetToken)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .build();

            // sendAsync를 통해 비동기로 전송
            FirebaseMessaging.getInstance().sendAsync(message)
                    .addListener(() -> log.info("Successfully sent message to {}", targetToken), Runnable::run);

        } catch (Exception e) {
            log.error("Error sending message to {}: {}", targetToken, e.getMessage());
        }
    }

}