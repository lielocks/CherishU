package cherish.backend.member.email.service;


import org.springframework.scheduling.annotation.Async;

public interface EmailService {
    @Async
    void sendMessage(String to, String code);
}
