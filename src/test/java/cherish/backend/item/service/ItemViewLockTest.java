package cherish.backend.item.service;

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

    private void executeMultiThread(int numberOfExecutions, RunnableWithException action, int threadPoolSize) throws InterruptedException {
        AtomicInteger successCount = new AtomicInteger();
        ExecutorService service = Executors.newFixedThreadPool(threadPoolSize);
        CountDownLatch latch = new CountDownLatch(numberOfExecutions);

        var startTime = System.currentTimeMillis();

        for (int i = 0; i < numberOfExecutions; i++) {
            service.execute(() -> {
                try {
                    action.run(); // 예외(InterruptedException) 를 처리할 수 있는 커스텀 함수형 인터페이스 사용
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
        var duration = (System.currentTimeMillis() - startTime);
        double seconds = duration / 1000.0;

        assertThat(successCount.get()).isEqualTo(numberOfExecutions);
        System.out.printf("Time Taken %.2f sec", seconds);
    }

    @FunctionalInterface
    interface RunnableWithException {
        void run() throws Exception;
    }

    @Test
    void increaseViewWithPessimisticLockForMultiThreadTest() throws InterruptedException {
        executeMultiThread(100, () -> itemService.increaseViewsWithLock(1L), 10);
    }

    @Test
    void increaseViewWithUpdateForMultiThreadTest() throws InterruptedException {
        executeMultiThread(100, () -> itemService.increaseViewsUpdating(1L), 10);
    }

    @Test
    void increaseViewsRequestLockFacade() throws InterruptedException {
        executeMultiThread(100, () -> itemService.increaseViewsRequestLock(1L), 32);
    }
}
