package cherish.backend.common.service;

import cherish.backend.common.constant.FirebaseProperties;
import cherish.backend.common.dto.FcmMessage;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.common.net.HttpHeaders;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
@RequiredArgsConstructor
public class FirebaseCloudMessageService {

    private final String API_URL = "https://fcm.googleapis.com/v1/projects/" + "cherishufcm/messages:send";
    private final FirebaseProperties firebaseProperties;
    private final ObjectMapper objectMapper;

    @Async
    public void sendMessageTo(String targetToken, String title, String body) throws IOException {

        String message = makeMessage(targetToken, title, body);
        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = RequestBody.create(message, MediaType.get("application/json; charset=utf-8"));

        Request request = new Request.Builder()
                .url(API_URL)
                .post(requestBody)
                .addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + getAccessToken())
                .addHeader(HttpHeaders.CONTENT_TYPE, "application/json; UTF-8")
                .build();

        Response response = client.newCall(request).execute();
        log.info("response {}", response.body().string());
    }

    private String makeMessage(String targetToken, String title, String body) throws JsonParseException, JsonProcessingException {
        FcmMessage fcmMessage = FcmMessage.builder()
                .message(FcmMessage.Message.builder()
                        .token(targetToken)
                        .notification(FcmMessage.Notification.builder()
                                .title(title)
                                .body(body)
                                .build()
                        ).build()).validateOnly(false).build();

        return objectMapper.writeValueAsString(fcmMessage);
    }

//    private String getAccessToken() throws IOException {
//        String firebaseConfigPath = "firebase/firebase_service_key.json";
//
//        GoogleCredentials googleCredentials = GoogleCredentials
//                .fromStream(new ClassPathResource(firebaseConfigPath).getInputStream())
//                .createScoped(List.of("https://www.googleapis.com/auth/cloud-platform"));
//
//        googleCredentials.refreshIfExpired();
//        return googleCredentials.getAccessToken().getTokenValue();
//    }

    public String getAccessToken() throws IOException {
        String firebaseConfig = String.format(
                "{\n" +
                        "  \"type\": \"%s\",\n" +
                        "  \"project_id\": \"%s\",\n" +
                        "  \"private_key_id\": \"%s\",\n" +
                        "  \"private_key\": \"%s\",\n" +
                        "  \"client_email\": \"%s\",\n" +
                        "  \"client_id\": \"%s\",\n" +
                        "  \"auth_uri\": \"%s\",\n" +
                        "  \"token_uri\": \"%s\",\n" +
                        "  \"auth_provider_x509_cert_url\": \"%s\",\n" +
                        "  \"client_x509_cert_url\": \"%s\"\n" +
                        "}",
                firebaseProperties.getType(),
                firebaseProperties.getProjectId(),
                firebaseProperties.getPrivateKeyId(),
                firebaseProperties.getPrivateKey().replace("\n", "\\n"),
                firebaseProperties.getClientEmail(),
                firebaseProperties.getClientId(),
                firebaseProperties.getAuthUri(),
                firebaseProperties.getTokenUri(),
                firebaseProperties.getAuthProviderX509CertUrl(),
                firebaseProperties.getClientX509CertUrl()
        );

        GoogleCredentials googleCredentials = GoogleCredentials
                .fromStream(new ByteArrayInputStream(firebaseConfig.getBytes()))
                .createScoped(List.of("https://www.googleapis.com/auth/cloud-platform"));

        googleCredentials.refreshIfExpired();
        return googleCredentials.getAccessToken().getTokenValue();
    }
}