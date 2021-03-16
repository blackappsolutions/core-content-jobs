package de.bas.contentsync.cae;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.Assert.*;

/**
 * @author mschwarz
 */
public class ContentSyncListenerTest {

    public static class SquareCalculator {

        private ExecutorService executor = Executors.newSingleThreadExecutor();

        public Future<Integer> calculate(Integer input) {
            return executor.submit(() -> {
                Thread.sleep(1000);
                return input * input;
            });
        }
    }

    @Test
    public void contentCheckedIn() throws Exception{

        Future<Integer> future = new SquareCalculator().calculate(10);

        while(!future.isDone()) {
            System.out.println("Calculating...");
            Thread.sleep(300);
        }

        Integer result = future.get();
        System.out.println(result);

        List<Integer> list = new ArrayList<>();
        list.add(1);
        list.add(2);
        list.add(3);
        list.removeIf(i -> i == 2);
        System.out.println(list);
    }
}
