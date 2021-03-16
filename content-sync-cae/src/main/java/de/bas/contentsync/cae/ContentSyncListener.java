package de.bas.contentsync.cae;

import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.content.events.ContentCheckedInEvent;
import com.coremedia.cap.content.events.ContentRepositoryListenerBase;
import com.coremedia.cap.content.query.QueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

/**
 * @author Markus Schwarz
 */
@Component
public class ContentSyncListener extends ContentRepositoryListenerBase {

    private static final Logger LOG = LoggerFactory.getLogger(ContentSyncListener.class);

    private static final String JOB_CONTENT_TYPE = "ContentSync";

    private final ContentRepository contentRepository;

    @Value("${initial.query}")
    private String initialQuery;

    private List<FutureTask<ContentSync>> taskList = new ArrayList<>();
    private static int PARALLEL_THREADS = 10;
    private ExecutorService executor = Executors.newFixedThreadPool(PARALLEL_THREADS);


    public ContentSyncListener(ContentRepository contentRepository, TaskScheduler taskScheduler) {
        this.contentRepository = contentRepository;


        Calendar startTwoMinutesInTheFuture = Calendar.getInstance();
        startTwoMinutesInTheFuture.add(Calendar.MINUTE, 2);
        // https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/scheduling/TaskScheduler.html#scheduleAtFixedRate-java.lang.Runnable-java.util.Date-long-
        taskScheduler.scheduleAtFixedRate(() -> {
            // Wait until all results are available and combine them at the same time
            for (int j = 0; j < PARALLEL_THREADS; j++) {
                FutureTask<ContentSync> futureTask = taskList.get(j);
                try {
                    ContentSync contentSync = futureTask.get();
                    LOG.info("FutureTask {} is done? {}", contentSync.getContentId(), futureTask.isDone());
                } catch (Exception e) {
                    LOG.error("Can not read futureTask!!!", e);
                }
            }
        }, startTwoMinutesInTheFuture.getTime(), (1000 * 60 * 2) /* every 2 Minutes */);
    }


    @Override
    public void contentCheckedIn(ContentCheckedInEvent event) {
        Content content = event.getContent();
        handleContentSyncResource(content);
    }

    private void handleContentSyncResource(Content content) {
        if (JOB_CONTENT_TYPE.equals(content.getType().getName())) {
            ContentSync contentSync = new ContentSync(content);
            if (contentSync.isActive()) {
                startJobThread(contentSync);
            }
        }
    }

    private void startJobThread(ContentSync contentSync) {
        FutureTask<ContentSync> futureTask = new FutureTask<>(() -> {
            LOG.info("Syncing ...");
            doTheSync();
            LOG.info("... done");
            contentSync.setlastRun(true);
            return contentSync;
        });
        taskList.add(futureTask);
        executor.execute(futureTask);
    }

    private void doTheSync() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    @PostConstruct
    public void afterPropertiesSet() throws Exception {
        QueryService queryService = contentRepository.getQueryService();
        Collection<Content> activeContentSyncs = queryService.poseContentQuery(initialQuery);
        for (Content content : activeContentSyncs) {
            handleContentSyncResource(content);
        }
        contentRepository.addContentRepositoryListener(this);
    }

    @PreDestroy
    public void cleanup() {
        executor.shutdown();
    }
}
