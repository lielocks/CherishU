//package cherish.backend.test;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.util.Map;
//import java.util.TreeMap;
//
//@RequestMapping("/test")
//@RestController
//public class HealthCheckController {
//
//    @Value("${server.env}")
//    private String env;
//
//    @Value("${server.port}")
//    private String serverPort;
//
//    @Value("${serverName}")
//    private String serverName;
//
//    @GetMapping("/hc")
//    public ResponseEntity<?> healthCheck() {
//        Map<String, String> responseData = new TreeMap<>();
//        responseData.put("serverName ", serverName);
//        responseData.put("serverPort ", serverPort);
//        responseData.put("env ", env);
//
//        return ResponseEntity.ok(responseData);
//    }
//
//    @GetMapping("/env")
//    public ResponseEntity<?> getEnv() {
//        return ResponseEntity.ok(env);
//    }
//
//}
