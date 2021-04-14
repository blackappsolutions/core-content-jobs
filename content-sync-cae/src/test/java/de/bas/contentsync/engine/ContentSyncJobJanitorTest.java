package de.bas.contentsync.engine;

import de.bas.contentsync.beans.ContentSync;
import de.bas.contentsync.beans.RepeatEvery;
import de.bas.contentsync.jobs.ContentSyncJob;
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
import org.springframework.context.annotation.PropertySource;
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
@ContextConfiguration(classes = ContentSyncJobJanitorTest.LocalConfig.class)
public class ContentSyncJobJanitorTest {

    @Mock
    ContentSync contentSync;

    @Mock
    ContentSyncJob contentSyncJob;

    @Mock
    ScheduledExecutorService executor;

    @Inject
    @InjectMocks
    ContentSyncJobJanitor contentSyncJobJanitor;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        Mockito.when(contentSyncJob.getContentSync()).thenReturn(contentSync);
    }

    @Configuration
    @PropertySource({"classpath:/content-sync.properties"})
    public static class LocalConfig {

        @Bean
        public TaskScheduler myTaskScheduler() {
            ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
            scheduler.setThreadNamePrefix("explicitScheduler-");
            return scheduler;
        }

        @Bean
        public ContentSyncJobJanitor contentSyncJobJanitor(TaskScheduler scheduler) {
            ContentSyncJobJanitor contentSyncJobJanitor = new ContentSyncJobJanitor(scheduler);
            contentSyncJobJanitor.cleanupPeriod = Duration.ofMillis(10);
            contentSyncJobJanitor.startupDelay = 1;  // lowered delay to one second to speed up testcase execution
            return contentSyncJobJanitor;
        }
    }

    @Test
    public void testJobMaintenance() throws Exception {
        contentSyncJobJanitor.execute(contentSyncJob);
        TimeUnit.SECONDS.sleep(contentSyncJobJanitor.startupDelay);
        Assert.assertEquals(0, contentSyncJobJanitor.getTaskList().size());
    }

    @Test
    public void testFutureDate() {
        Calendar instance = Calendar.getInstance();
        instance.add(Calendar.SECOND, 30);
        Assert.assertNotNull(contentSyncJobJanitor.startScheduled(contentSyncJob, instance));
        Mockito.verify(executor, times(1)).schedule(any(ContentSyncJob.class), anyLong(), any());
    }

    @Test
    public void testPastDate() {
        Calendar instance = Calendar.getInstance();
        instance.add(Calendar.SECOND, -30);
        Assert.assertNull(contentSyncJobJanitor.startScheduled(contentSyncJob, instance));
    }

    @Test
    public void testGetRepetitionRateMillis() {
        Assert.assertEquals(contentSyncJobJanitor.getRepetitionRateMillis(RepeatEvery.HOUR), 60 * 60 * 1000);
        Assert.assertEquals(contentSyncJobJanitor.getRepetitionRateMillis(RepeatEvery.DAY),  24 * 60 * 60 * 1000);
        Assert.assertEquals(contentSyncJobJanitor.getRepetitionRateMillis(RepeatEvery.WEEK), 7 * 24 *60 * 60 * 1000);
    }
}
