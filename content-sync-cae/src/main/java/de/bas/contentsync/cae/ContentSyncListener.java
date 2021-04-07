package de.bas.contentsync.cae;

import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.content.events.ContentCheckedInEvent;
import com.coremedia.cap.content.events.ContentRepositoryListenerBase;
import com.coremedia.cap.content.query.QueryService;
import com.coremedia.objectserver.beans.ContentBeanFactory;
import de.bas.contentsync.beans.ContentSync;
import de.bas.contentsync.jobs.ContentSyncJob;
import de.bas.contentsync.jobs.ExportXMLJob;
import de.bas.contentsync.jobs.ImportRSSJob;
import de.bas.contentsync.jobs.ImportXMLJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Collection;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static de.bas.contentsync.beans.ContentSync.CONTENTTYPE_CONTENTSYNC;

/**
 * @author Markus Schwarz
 */
@Component
@ConditionalOnProperty(name = "delivery.preview-mode", havingValue = "true")
public class ContentSyncListener extends ContentRepositoryListenerBase {

    private static final Logger LOG = LoggerFactory.getLogger(ContentSyncListener.class);
    
    @Value("${initial.query}")
    private String initialQuery;

    private static final int PARALLEL_THREADS = 10;
    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(PARALLEL_THREADS);
    private final ContentWriter contentWriter;
    private final ContentRepository contentRepository;
    private final ContentBeanFactory contentBeanFactory;
    // private final ContentSyncJobJanitor contentSyncJobJanitor;

    public ContentSyncListener(@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection") ContentRepository contentRepository,
                               ContentWriter contentWriter,
                               ContentBeanFactory contentBeanFactory/*,
                               ContentSyncJobJanitor contentSyncJobJanitor*/) {
        this.contentRepository = contentRepository;
        this.contentWriter = contentWriter;
        this.contentBeanFactory = contentBeanFactory;
        // this.contentSyncJobJanitor = contentSyncJobJanitor;
    }

    @Override
    public void contentCheckedIn(ContentCheckedInEvent event) {
        Content content = event.getContent();
        if (CONTENTTYPE_CONTENTSYNC.equals(content.getType().getName())) {
            handleContentSyncResource(content);
        }
    }

    private void handleContentSyncResource(Content content) {
        ContentSync contentSync = contentBeanFactory.createBeanFor(content, ContentSync.class);
        if (contentSync.isActive()) {
            startJobThread(contentSync);
        }
    }

    private void startJobThread(ContentSync contentSync) {
        // ToDo: Improve that to a more object-oriented way
        ContentSyncJob contentSyncJob = null;
        switch (contentSync.getType()){
            case ServerExport:
                contentSyncJob = new ExportXMLJob(contentSync, contentWriter);
                break;
            case ServerImport:
                contentSyncJob = new ImportXMLJob(contentSync, contentWriter);
                break;
            case ImportRSS:
                contentSyncJob = new ImportRSSJob(contentSync, contentWriter);
                break;
        }
        FutureTask<ContentSync> futureTask = new FutureTask<>(contentSyncJob);
        executor.schedule(futureTask, 5, TimeUnit.SECONDS); // delay execution a bit ...
        // contentSyncJobJanitor.add(futureTask);
    }

    @PostConstruct
    public void afterPropertiesSet() {
        if (contentRepository.isContentManagementServer()) {
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
    @PreDestroy
    public void cleanup() {
        executor.shutdown();
    }
}
