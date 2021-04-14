package de.bas.contentsync.engine;

import com.twelvemonkeys.lang.StringUtil;
import de.bas.contentsync.beans.RepeatEvery;
import de.bas.contentsync.jobs.ContentSyncJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import static com.coremedia.blueprint.base.links.UriConstants.Segments.PREFIX_DYNAMIC;
import static com.coremedia.blueprint.base.links.UriConstants.Segments.SEGMENT_ID;
import static java.util.concurrent.TimeUnit.DAYS;
import static java.util.concurrent.TimeUnit.HOURS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

@Slf4j
@RequestMapping
@Component("contentSyncJobJanitor")
@ConditionalOnProperty(name = "delivery.preview-mode", havingValue = "true")
public class ContentSyncJobJanitor {

    int startupDelay = 5; // 5 secs
    Duration cleanupPeriod = Duration.ofDays(1); // cleanup once a day

    /**
     * URI pattern, for URIs like "/dynamic/content-sync-jobs/terminate/1234"
     */
    public static final String DYNAMIC_URI_PATTERN = '/' + PREFIX_DYNAMIC + "/content-sync-jobs/terminate" + "/{" + SEGMENT_ID + '}';
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

    public void add(ScheduledFuture<?> futureTask, ContentSyncJob contentSyncJob) {
        synchronized (taskList) {
            taskList.add(new ScheduledFutureHolder(futureTask, contentSyncJob));
        }
    }

    @GetMapping(value = DYNAMIC_URI_PATTERN)
    public Object handleFragmentRequest(@PathVariable(SEGMENT_ID) String contentId,
                                        @RequestParam(value = "origUrl", required = false) String origUrl) {
        removeAlreadyScheduledJob(Integer.parseInt(contentId));

        if (StringUtil.isEmpty(origUrl)) {
            return "Job cancelled";
        }

        return new RedirectView(origUrl);
    }

    public List<ScheduledFutureHolder> getTaskList() {
        synchronized (taskList) {
            return taskList;
        }
    }

    public void execute(ContentSyncJob contentSyncJob) {
        ScheduledFuture<?> scheduledFuture = getScheduledFuture(contentSyncJob);
        if (scheduledFuture != null) {
            add(scheduledFuture, contentSyncJob);
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
        int contentId = job.getContentSync().getContentId();
        Long millisUntilLaunch = getMillisUntilLaunch(startAt, contentId);
        if (millisUntilLaunch == null) return null;

        removeAlreadyScheduledJob(job.getContentSync().getContentId());

        log.info("Starting Content-Sync Job {} in {}ms ", contentId, millisUntilLaunch);

        RepeatEvery repetition = job.getContentSync().getRepetition();

        if (repetition == null) {
            return executor.schedule(job, millisUntilLaunch, MILLISECONDS);
        }

        if (job instanceof Runnable) {
            Runnable rJob = (Runnable) job;
            long repetitionRateMillis = getRepetitionRateMillis(repetition);
            return executor.scheduleAtFixedRate(rJob, millisUntilLaunch, repetitionRateMillis, MILLISECONDS);
        }

        log.error("Recurring job {} does not implement Runnable! " +
            "Please configure another type of job or fix that in the code.", job.getContentSync().getType());

        return null;
    }

    private Long getMillisUntilLaunch(Calendar startAt, int contentId) {
        long now = System.currentTimeMillis();
        long then = startAt.getTimeInMillis();
        long millisUntilLaunch = then - now;
        if (millisUntilLaunch < 0) {
            log.warn(
                "Can not start Content-Sync Job {}. Start time has elapsed! now={} then={} (startAt={})",
                contentId, now, then, startAt
            );
            return null;
        }
        return millisUntilLaunch;
    }

    long getRepetitionRateMillis(RepeatEvery repetition) {
        long repetitionRateMillis = 0;
        switch (repetition) {
            case HOUR:
                repetitionRateMillis = HOURS.toMillis(1);
                break;
            case DAY:
                repetitionRateMillis = DAYS.toMillis(1);
                break;
            case WEEK:
                repetitionRateMillis = DAYS.toMillis(7);
                break;
        }
        return repetitionRateMillis;
    }

    private void removeAlreadyScheduledJob(int contentId) {
        synchronized (taskList) {
            taskList.removeIf(entry -> {
                if (entry.contentSyncJob.getContentSync().getContentId() == contentId) {
                    if (!entry.future.isDone()) { // waits for its start
                        entry.future.cancel(true);
                        entry.getContentSyncJob().cancel();
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
