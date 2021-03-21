package de.bas.contentsync.cae;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author mschwarz
 */
public class ContentSyncListenerTest {

    public static class SquareCalculator {

        private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(4);

        public Future<Integer> calculate(Integer input) {
            System.out.println("Planned: " + Calendar.getInstance());
            return executor.schedule(() -> {
                System.out.println("Started: " + Calendar.getInstance());
                Thread.sleep(1000);
                return input * input;
            }, 5, TimeUnit.SECONDS);
        }
    }

    @Test
    public void contentCheckedIn() throws Exception {
        Future<Integer> future0 = new SquareCalculator().calculate(10);
        Future<Integer> future1 = new SquareCalculator().calculate(10);
        Future<Integer> future2 = new SquareCalculator().calculate(10);
        Future<Integer> future3 = new SquareCalculator().calculate(10);

        List<Future<Integer>> list = new ArrayList<>();

        list.add(future0);
        list.add(future1);
        list.add(future2);
        list.add(future3);

        for (Future<Integer> future : list) {
            if (future.isDone()) {
                Integer result = future.get();
                System.out.println(result);
            } else {
                while (!future.isDone()) {
                    System.out.println("Waiting 0.3s ..");
                    Thread.sleep(300);
                }
            }
        }


//        List<Integer> list1 = new ArrayList<>();
//        list1.add(1);
//        list1.add(2);
//        list1.add(3);
//        list1.removeIf(i -> i == 2);
//        System.out.println(list1);
    }
}
