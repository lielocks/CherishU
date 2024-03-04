package cherish.backend.test;

import cherish.backend.common.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Profile("local")
@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/test")
public class TestRestController {

    private final RedisService redisService;

    @GetMapping("excuteProcess/{processNum}")
    public String process(@PathVariable int processNum) throws InterruptedException {
        log.info("========================== Start Process -> Process Number: {}", processNum);
        Thread.sleep(20000);
        log.info("========================== End Process -> Process Number: {}", processNum);

        return "Process Success !!";
    }

    @PostMapping("/redis")
    public void testRedisPost() {
        redisService.setEmailVerified("test", true, 500);
    }

    @GetMapping("/redis")
    public boolean testRedis() {
        return redisService.isEmailVerified("test");
    }

    @GetMapping
    public String test() {
        return "ok";
    }

    @GetMapping("/date")
    public LocalDate testDate(@RequestParam("date") LocalDate date){
        return date;
    }
}
