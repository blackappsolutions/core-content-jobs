package de.bas.contentsync.engine;

import de.bas.contentsync.beans.ContentSync;
import de.bas.contentsync.jobs.ContentSyncJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component("contentSyncJobJanitor")
@ConditionalOnProperty(name = "delivery.preview-mode", havingValue = "true")
public class ContentSyncJobJanitor {

    int startupDelay = 5; // 5 secs
    Duration cleanupPeriod = Duration.ofDays(1); // cleanup once a day

    private static final int PARALLEL_THREADS = 10;
    @SuppressWarnings("FieldMayBeFinal") //non-final for mockito testcase usage
    private ScheduledExecutorService executor = Executors.newScheduledThreadPool(PARALLEL_THREADS);
    private final List<ScheduledFutureHolder> taskList = new ArrayList<>();

    private final Runnable removeAndLogFinishedJobs = () -> {
        synchronized (taskList) {
            taskList.removeIf(entry -> entry.future.isDone());
        }
    };
    final TaskScheduler taskScheduler;

    public ContentSyncJobJanitor(@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection") TaskScheduler taskScheduler) {
        this.taskScheduler = taskScheduler;
    }

    @PostConstruct
    public void afterPropertiesSet() {
        taskScheduler.scheduleAtFixedRate(removeAndLogFinishedJobs, cleanupPeriod);
    }

    public void add(ScheduledFuture<?> futureTask, ContentSync contentSync) {
        synchronized (taskList) {
            taskList.add(new ScheduledFutureHolder(futureTask, contentSync));
        }
    }

    public List<ScheduledFutureHolder> getTaskList() {
        synchronized (taskList) {
            return taskList;
        }
    }

    public void execute(ContentSyncJob contentSyncJob) {
        ScheduledFuture<?> scheduledFuture = getScheduledFuture(contentSyncJob);
        if (scheduledFuture != null) {
            add(scheduledFuture, contentSyncJob.getContentSync());
        }
    }

    private ScheduledFuture<?> getScheduledFuture(ContentSyncJob job) {
        Calendar startAt = job.getContentSync().getStartAt();
        if (startAt == null) {
            // delay execution a bit, because if task completes too fast, we might confuse the user
            startAt = Calendar.getInstance();
            startAt.add(Calendar.SECOND, startupDelay);
        }
        return startScheduled(job, startAt);
    }

    ScheduledFuture<?> startScheduled(ContentSyncJob job, Calendar startAt) {
        long now = System.currentTimeMillis();
        long then = startAt.getTimeInMillis();
        long millisUntilLaunch = then - now;
        int contentId = job.getContentSync().getContentId();
        if (millisUntilLaunch < 0) {
            log.warn(
                "Can not start Content-Sync Job {}. Start time has elapsed! now={} then={} (startAt={})",
                contentId, now, then, startAt
            );
            return null;
        }

        removeAlreadyScheduledJob(job);

        log.info("Starting Content-Sync Job {} in {}ms", contentId, millisUntilLaunch);
        return executor.schedule(job, millisUntilLaunch, TimeUnit.MILLISECONDS);
    }

    private void removeAlreadyScheduledJob(ContentSyncJob job) {
        synchronized (taskList) {
            taskList.removeIf(entry -> {
                if(entry.contentSync.getContentId() == job.getContentSync().getContentId()){
                    if(!entry.future.isDone()){ // waits for its start
                        entry.future.cancel(true);
                    }
                    return true;
                }
                return false;
            });
        }
    }

    @PreDestroy
    public void cleanup() {
        executor.shutdown();
    }
}
