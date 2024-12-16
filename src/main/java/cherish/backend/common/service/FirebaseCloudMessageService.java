package cherish.backend.common.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;


@Component
@Slf4j
@RequiredArgsConstructor
public class FirebaseCloudMessageService {

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