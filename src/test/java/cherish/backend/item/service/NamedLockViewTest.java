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
import java.util.concurrent.atomic.AtomicInteger;

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
        AtomicInteger successCount = new AtomicInteger();
        ExecutorService service = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(100);

        for (int i = 0; i < 100; i++) {
            service.execute(() -> {
                try {
                    lockFacade.increase(1L);
                    successCount.getAndIncrement();
                } catch (Exception e) {
                    System.out.println(e);
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();

        System.out.println("views : " + itemRepository.findById(1L).get().getViews());
    }
}
