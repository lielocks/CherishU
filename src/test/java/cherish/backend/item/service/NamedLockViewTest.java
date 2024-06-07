package cherish.backend.item.service;

import cherish.backend.item.model.Item;
import cherish.backend.item.repository.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class NamedLockViewTest {

    @Autowired
    NamedLockViewFacade lockFacade;

    @Autowired
    ItemRepository itemRepository;

    @BeforeEach
    void setUp() {
        itemRepository.saveAndFlush(
                Item.builder().id(1L).views(0).brand("Aesop").build());
    }

    @Test
    void namedLockForMultiThreadTest() throws InterruptedException {

        int numberOfExecute = 100;
        ExecutorService service = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(numberOfExecute);

        var startTime = System.currentTimeMillis();

        for (int i = 0; i < numberOfExecute; i++) {
            service.submit(() -> {
                try {
                    lockFacade.increase(1L);
                } catch (Exception e) {
                    System.out.println(e);
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        var endTime = System.currentTimeMillis();

        System.out.println("view "  + itemRepository.findById(1L).get().getViews());
        System.out.println("TIME TAKEN FOR NAMED LOCK TEST : " + (endTime - startTime));
    }
}
