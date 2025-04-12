package cherish.backend.common.service;

import cherish.backend.common.dto.FcmTokenRequestDto;
import com.google.common.util.concurrent.RateLimiter;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.concurrent.*;


@Component
@Slf4j
@RequiredArgsConstructor
public class FirebaseCloudMessageService {

    private final BlockingQueue<FcmTokenRequestDto> queue = new LinkedBlockingQueue<>(5000); // 큐 최대 사이즈
    private final ExecutorService executorService = Executors.newFixedThreadPool(5);
    private final RateLimiter rateLimiter = RateLimiter.create(80);

    @PostConstruct
    public void init() {
        for (int i = 0; i < 5; i++) {
            executorService.submit(this::processQueue);
        }
    }

    public void enqueueMessage(FcmTokenRequestDto dto) {
        try {
            boolean success = queue.offer(dto, 50, TimeUnit.MILLISECONDS);
            if (!success) {
                log.warn("Queue offer 실패! Delaying message to : {}", dto.getTargetToken());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Queue offer interrupted : {}", e.getMessage(), e);
        }
    }

    private void processQueue() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                FcmTokenRequestDto dto = queue.poll(100, TimeUnit.MILLISECONDS); // 없으면 잠깐 쉼
                if (dto != null) {
                    sendMessage(dto);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("Unexpected error in queue processing: {}", e.getMessage(), e);
            }
        }
    }

    private void sendMessage(FcmTokenRequestDto dto) {
        try {
            if (rateLimiter.tryAcquire(10, TimeUnit.MILLISECONDS)) { // burst 완화
                Message message = Message.builder()
                        .setToken(dto.getTargetToken())
                        .setNotification(Notification.builder()
                                .setTitle(dto.getTitle())
                                .setBody(dto.getBody())
                                .build())
                        .build();

                String response = FirebaseMessaging.getInstance().send(message);
                log.info("Successfully sent to {}: {}", dto.getTargetToken(), response);
            } else {
                log.warn("Rate limit exceeded. Dropping or delaying message to {}", dto.getTargetToken());
            }
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