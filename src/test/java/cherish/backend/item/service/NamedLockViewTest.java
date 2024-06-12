package cherish.backend.item.service;

import cherish.backend.item.model.Item;
import cherish.backend.item.repository.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

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
        int numThreads = 10;
        CountDownLatch latch = new CountDownLatch(numThreads);
        Executor taskExecutor = Executors.newFixedThreadPool(numThreads);

        for (int i = 0; i < numThreads; i++) {
            taskExecutor.execute(() -> {
                lockFacade.increase(1L);
                latch.countDown();
            });
        }

        latch.await();
        Item updatedItem = itemRepository.findById(1L).orElseThrow();
        System.out.println("views : " + updatedItem.getViews());
    }
}
