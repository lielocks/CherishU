package cherish.backend.item.controller;

import cherish.backend.item.model.Item;
import cherish.backend.item.repository.ItemRepository;
import cherish.backend.item.service.ItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ItemViewLockTest {

    @Autowired
    ItemService itemService;

    @Autowired
    ItemRepository itemRepository;

    @BeforeEach
    void setUp() {
        itemRepository.save(
                Item.builder().id(1L).views(0).brand("Aesop").build());
    }

    @Test
    void increaseViewWithPessimisticLockForMultiThreadTest() throws InterruptedException {

        AtomicInteger successCount = new AtomicInteger();
        int numberOfExecute = 100;
        ExecutorService service = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(numberOfExecute);

        var startTime = System.currentTimeMillis();

        for (int i = 0; i < numberOfExecute; i++) {
            service.execute(() -> {
                try {
                    itemService.increaseViewsWithLock(1L);
                    successCount.getAndIncrement();
                    System.out.println("SUCCESS");
                } catch (Exception e) {
                    System.out.println(e);
                }

                latch.countDown();
            });
        }
        latch.await();
        var endTime = System.currentTimeMillis();

        assertThat(successCount.get()).isEqualTo(100);
        System.out.println("TIME TAKEN FOR PESSIMISTIC WRITE TEST : " + (endTime - startTime));
    }


    @Test
    void increaseViewWithUpdateForMultiThreadTest() throws InterruptedException {

        AtomicInteger successCount = new AtomicInteger();
        int numberOfExecute = 100;
        ExecutorService service = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(numberOfExecute);

        var startTime = System.currentTimeMillis();

        for (int i = 0; i < numberOfExecute; i++) {
            service.execute(() -> {
                try {
                    itemService.increaseViewsUpdating(1L);
                    successCount.getAndIncrement();
                    System.out.println("SUCCESS");
                } catch (Exception e) {
                    System.out.println(e);
                }

                latch.countDown();
            });
        }
        latch.await();
        var endTime = System.currentTimeMillis();

        assertThat(successCount.get()).isEqualTo(100);
        System.out.println("TIME TAKEN FOR UPDATE MODIFYING TEST : " + (endTime - startTime));
    }

    @Test
    void increaseViewsRequestLockFacade() throws InterruptedException {

        AtomicInteger successCount = new AtomicInteger();
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        var startTime = System.currentTimeMillis();
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    itemService.increaseViewsRequestLock(1L);
                    successCount.getAndIncrement();
                    System.out.println("SUCCESS");
                } catch (Exception e) {
                    System.out.println(e);
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();

        var endTime = System.currentTimeMillis();

        assertThat(successCount.get()).isEqualTo(100);
        System.out.println("TIME TAKEN FOR UPDATE MODIFYING TEST : " + (endTime - startTime));
    }

}