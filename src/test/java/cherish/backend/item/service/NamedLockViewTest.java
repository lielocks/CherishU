package cherish.backend.item.service;

import cherish.backend.item.model.Item;
import cherish.backend.item.repository.ItemFilterRepository;
import cherish.backend.item.repository.ItemRepository;
import cherish.backend.item.repository.LockRepository;
import cherish.backend.item.repository.NamedLockRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static cherish.backend.util.ExecutorUtils.printState;
import static cherish.backend.util.MyLogger.log;

@SpringBootTest
public class NamedLockViewTest {

    @Autowired
    NamedLockRepository lockRepository;

    @Autowired
    ItemRepository itemRepository;

    @Autowired
    ItemService itemService;

    @Autowired
    ItemFilterRepository itemFilterRepository;

    @BeforeEach
    void setUp() {
        itemRepository.saveAndFlush(
                Item.builder().id(1L).views(0).brand("Aesop").build());
    }

    @Test
    void namedLockForMultiThreadTest() throws InterruptedException {
        AtomicInteger successCount = new AtomicInteger();
        ExecutorService es = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(100);

        for (int i = 0; i < 100; i++) {
            es.execute(() -> {
                try {
                    lockRepository.increaseViews(1L);
                    successCount.getAndIncrement();
                } catch (Exception e) {
                    System.out.println(e);
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        es.shutdown();

        System.out.println("views : " + itemRepository.findById(1L).get().getViews());
    }

    @Test
    void threadPoolMultiThreadTest() {
        ExecutorService es =
                new ThreadPoolExecutor(10, 20, 60, TimeUnit.SECONDS, new ArrayBlockingQueue<>(100));
        printState(es);

        long startMs = System.currentTimeMillis();

        try {
            es.execute(new viewTask(1, 100));
            printState(es);
        } catch (RejectedExecutionException e) {
            log(e);
        }

        printState(es);
//        es.shutdown();
//        long endMs = System.currentTimeMillis();
//        log("time: " + (endMs - startMs));
//
//        log("== shutdown 완료 ==");
    }

     static class viewTask implements Runnable {
         int startValue;
         int endValue;
         int result = 0;

         public viewTask(int startValue, int endValue) {
             this.startValue = startValue;
             this.endValue = endValue;
         }

         @Override
         public void run() {
             log("작업 시작");
//             try {
//                 Thread.sleep(2000);
//             } catch (InterruptedException e) {
//                 throw new RuntimeException(e);
//             }
             int sum = 0;
             for (int i = startValue; i <= endValue; i++) {
                 sum += i;
                 log("sum " + sum);
             }
             result = sum;
             log("작업 완료 result=" + result);
         }
    }

}
