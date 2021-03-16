package de.bas.contentsync.cae;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.FutureTask;

/**
 * @author Markus Schwarz
 */
@Component
public class ContentSyncJobJanitor {

    private static final Logger LOG = LoggerFactory.getLogger(ContentSyncJobJanitor.class);

    private final TaskScheduler taskScheduler;
    private final List<FutureTask<ContentSync>> taskList = new ArrayList<>();
    private final Runnable removeAndLogFinishedJobs = () -> taskList.removeIf(task -> {
        boolean done = task.isDone();
        if (done) {
            try {
                LOG.info("FutureTask {} is done.", task.get().getContentId());
            } catch (Exception e) {
                LOG.error("Can not read futureTask!!!", e);
            }
        }
        return done;
    });

    public ContentSyncJobJanitor(TaskScheduler taskScheduler) {
        this.taskScheduler = taskScheduler;
    }

    @PostConstruct
    public void afterPropertiesSet() {
        Calendar startTwoMinutesInTheFuture = Calendar.getInstance();
        startTwoMinutesInTheFuture.add(Calendar.MINUTE, 2);
        // https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/scheduling/TaskScheduler.html#scheduleAtFixedRate-java.lang.Runnable-java.util.Date-long-
        taskScheduler.scheduleAtFixedRate(
            removeAndLogFinishedJobs,
            startTwoMinutesInTheFuture.getTime(),
            (1000 * 60 * 2) /* every 2 Minutes */
        );

    }

    public void add(FutureTask<ContentSync> futureTask) {
        taskList.add(futureTask);
    }
}
