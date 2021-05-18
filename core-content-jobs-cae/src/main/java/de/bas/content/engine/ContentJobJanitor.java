package de.bas.content.engine;

import com.twelvemonkeys.lang.StringUtil;
import de.bas.content.beans.RepeatEvery;
import de.bas.content.jobs.AbstractContentJob;
import lombok.extern.slf4j.Slf4j;
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
@Component("contentJobJanitor")
public class ContentJobJanitor {

    int startupDelay = 5; // 5 secs
    Duration cleanupPeriod = Duration.ofDays(1); // cleanup once a day

    /**
     * URI pattern, for URIs like "/dynamic/content-jobs/terminate/1234"
     */
    public static final String DYNAMIC_URI_PATTERN = '/' + PREFIX_DYNAMIC + "/content-jobs/terminate" + "/{" + SEGMENT_ID + '}';
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

    public ContentJobJanitor(@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection") TaskScheduler taskScheduler) {
        this.taskScheduler = taskScheduler;
    }

    @PostConstruct
    public void afterPropertiesSet() {
        taskScheduler.scheduleAtFixedRate(removeAndLogFinishedJobs, cleanupPeriod);
    }

    public void add(ScheduledFuture<?> futureTask, AbstractContentJob contentJob) {
        synchronized (taskList) {
            taskList.add(new ScheduledFutureHolder(futureTask, contentJob));
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

    public void execute(AbstractContentJob contentJob) {
        ScheduledFuture<?> scheduledFuture = getScheduledFuture(contentJob);
        if (scheduledFuture != null) {
            add(scheduledFuture, contentJob);
        }
    }

    private ScheduledFuture<?> getScheduledFuture(AbstractContentJob job) {
        Calendar startAt = job.getContentJobBean().getStartAt();
        if (startAt == null) {
            // delay execution a bit, because if task completes too fast, we might confuse the user
            startAt = Calendar.getInstance();
            startAt.add(Calendar.SECOND, startupDelay);
        }
        return startScheduled(job, startAt);
    }

    ScheduledFuture<?> startScheduled(AbstractContentJob job, Calendar startAt) {
        int contentId = job.getContentJobBean().getContentId();
        Long millisUntilLaunch = getMillisUntilLaunch(startAt, contentId);
        if (millisUntilLaunch == null) return null;

        removeAlreadyScheduledJob(job.getContentJobBean().getContentId());

        log.info("Starting Content-Job {} in {}ms ", contentId, millisUntilLaunch);

        RepeatEvery repetition = job.getContentJobBean().getRepetition();

        if (repetition == null) {
            return executor.schedule(job, millisUntilLaunch, MILLISECONDS);
        }

        if (job instanceof Runnable) {
            Runnable rJob = (Runnable) job;
            long repetitionRateMillis = getRepetitionRateMillis(repetition);
            return executor.scheduleAtFixedRate(rJob, millisUntilLaunch, repetitionRateMillis, MILLISECONDS);
        }

        log.error("Recurring job {} does not implement Runnable! " +
            "Please configure another type of job or fix that in the code.", job.getContentJobBean().getType());

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
                if (entry.abstractContentJob.getContentJobBean().getContentId() == contentId) {
                    if (!entry.future.isDone()) { // waits for its start
                        entry.future.cancel(true);
                        entry.getAbstractContentJob().cancel();
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
