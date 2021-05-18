package de.bas.content.engine;

import de.bas.content.beans.ContentJob;
import de.bas.content.beans.RepeatEvery;
import de.bas.content.jobs.AbstractContentJob;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import java.time.Duration;
import java.util.Calendar;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;

/**
 * @author mschwarz
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ContentJobJanitorTest.LocalConfig.class)
public class ContentJobJanitorTest {

    @Mock
    ContentJob contentJobBean;

    @Mock
    AbstractContentJob abstractContentJob;

    @Mock
    ScheduledExecutorService executor;

    @Inject
    @InjectMocks
    ContentJobJanitor contentJobJanitor;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        Mockito.when(abstractContentJob.getContentJobBean()).thenReturn(contentJobBean);
    }

    @Configuration
    public static class LocalConfig {

        @Bean
        public TaskScheduler myTaskScheduler() {
            ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
            scheduler.setThreadNamePrefix("explicitScheduler-");
            return scheduler;
        }

        @Bean
        public ContentJobJanitor contentJobJanitor(TaskScheduler scheduler) {
            ContentJobJanitor contentJobJanitor = new ContentJobJanitor(scheduler);
            contentJobJanitor.cleanupPeriod = Duration.ofMillis(10);
            contentJobJanitor.startupDelay = 1;  // lowered delay to one second to speed up testcase execution
            return contentJobJanitor;
        }
    }

    @Test
    public void testJobMaintenance() throws Exception {
        contentJobJanitor.execute(abstractContentJob);
        TimeUnit.SECONDS.sleep(contentJobJanitor.startupDelay);
        Assert.assertEquals(0, contentJobJanitor.getTaskList().size());
    }

    @Test
    public void testFutureDate() {
        Calendar instance = Calendar.getInstance();
        instance.add(Calendar.SECOND, 30);
        contentJobJanitor.startScheduled(abstractContentJob, instance);
        Mockito.verify(executor, times(1)).schedule(any(AbstractContentJob.class), anyLong(), any());
    }

    @Test
    public void testPastDate() {
        Calendar instance = Calendar.getInstance();
        instance.add(Calendar.SECOND, -30);
        contentJobJanitor.startScheduled(abstractContentJob, instance);
        Mockito.verify(executor, times(0)).schedule(any(AbstractContentJob.class), anyLong(), any());
    }

    @Test
    public void testGetRepetitionRateMillis() {
        Assert.assertEquals(contentJobJanitor.getRepetitionRateMillis(RepeatEvery.HOUR), 60 * 60 * 1000);
        Assert.assertEquals(contentJobJanitor.getRepetitionRateMillis(RepeatEvery.DAY),  24 * 60 * 60 * 1000);
        Assert.assertEquals(contentJobJanitor.getRepetitionRateMillis(RepeatEvery.WEEK), 7 * 24 *60 * 60 * 1000);
    }
}
