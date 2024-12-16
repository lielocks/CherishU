package cherish.backend.common.config;

import cherish.backend.common.constant.FirebaseProperties;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;

@Component
@RequiredArgsConstructor
public class FirebaseInitializer {

    private final FirebaseProperties firebaseProperties;

    @PostConstruct
    public void initializeFirebase() throws Exception {
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

        GoogleCredentials credentials = GoogleCredentials.fromStream(new ByteArrayInputStream(firebaseConfig.getBytes()));
        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(credentials)
                .setProjectId(firebaseProperties.getProjectId())
                .build();

        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseApp.initializeApp(options);
        }
    }

}
