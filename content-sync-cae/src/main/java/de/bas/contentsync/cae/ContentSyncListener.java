package de.bas.contentsync.cae;

import com.coremedia.cap.Cap;
import com.coremedia.cap.common.CapConnection;
import com.coremedia.cap.common.InvalidLoginException;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.content.events.ContentCheckedInEvent;
import com.coremedia.cap.content.events.ContentRepositoryListenerBase;
import com.coremedia.cap.content.query.QueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Collection;
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

    /**
     * We use a separate contentServer connection here, because we need to read AND write content.
     */
    private ContentRepository contentRepository;

    @Value("${initial.query}")
    private String initialQuery;
    @Value("${repository.url}")
    private String repoUrl;
    @Value("${content-sync.user}")
    private String user;
    @Value("${content-sync.pass}")
    private String pass;

    private static final int PARALLEL_THREADS = 10;
    private final ExecutorService executor = Executors.newFixedThreadPool(PARALLEL_THREADS);
    private final ContentSyncJobJanitor contentSyncJobJanitor;

    public ContentSyncListener(ContentSyncJobJanitor contentSyncJobJanitor) {
        this.contentSyncJobJanitor = contentSyncJobJanitor;
    }

    @Override
    public void contentCheckedIn(ContentCheckedInEvent event) {
        Content content = event.getContent();
        if (JOB_CONTENT_TYPE.equals(content.getType().getName())) {
            handleContentSyncResource(content);
        }
    }

    private void handleContentSyncResource(Content content) {
        ContentSync contentSync = new ContentSync(content);
        if (contentSync.isActive()) {
            startJobThread(contentSync);
        }
    }

    private void startJobThread(ContentSync contentSync) {
        FutureTask<ContentSync> futureTask = new FutureTask<>(new ContentSyncJob(contentSync));
        executor.execute(futureTask);
        contentSyncJobJanitor.add(futureTask);
    }

    @PostConstruct
    public void afterPropertiesSet() {
        initContentRepository();
        if ((contentRepository != null) && contentRepository.isContentManagementServer()) {
            executeInitialContentQuery();
            contentRepository.addContentRepositoryListener(this);
        }
    }

    private void executeInitialContentQuery() {
        QueryService queryService = contentRepository.getQueryService();
        Collection<Content> activeContentSyncs = queryService.poseContentQuery(initialQuery);
        LOG.info("Found {} active ContentSync resources", activeContentSyncs.size());
        for (Content content : activeContentSyncs) {
            handleContentSyncResource(content);
        }
    }

    // We have to use a separate connection with a user that allows us to write content
    private void initContentRepository() {
        try {
            CapConnection con = Cap.connect(repoUrl, user, pass);
            LOG.info("Opened connection for user {}", user);
            contentRepository = con.getContentRepository();
        } catch (InvalidLoginException e) {
            LOG.info("Can not log in user {} at {}. No worries if this happens on cae-live.", user, repoUrl);
        }
    }

    @PreDestroy
    public void cleanup() {
        executor.shutdown();
    }
}
