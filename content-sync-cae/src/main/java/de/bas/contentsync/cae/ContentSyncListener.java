package de.bas.contentsync.cae;

import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.content.events.ContentCheckedInEvent;
import com.coremedia.cap.content.events.ContentRepositoryListenerBase;
import com.coremedia.cap.content.query.QueryService;
import com.coremedia.objectserver.beans.ContentBeanFactory;
import de.bas.contentsync.beans.ContentSync;
import de.bas.contentsync.jobs.ContentSyncJob;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Collection;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static de.bas.contentsync.beans.ContentSync.CONTENTTYPE_CONTENTSYNC;

/**
 * @author Markus Schwarz
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "delivery.preview-mode", havingValue = "true")
public class ContentSyncListener extends ContentRepositoryListenerBase {

    private static final Logger LOG = LoggerFactory.getLogger(ContentSyncListener.class);

    @Value("${initial.query}")
    private String initialQuery;

    private final ContentRepository contentRepository;
    private final ContentBeanFactory contentBeanFactory;
    private final ApplicationContext appContext;
    private final ContentWriter contentWriter;
    private final ContentSyncJobJanitor contentSyncJobJanitor;

    public ContentSyncListener(@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection") ContentRepository contentRepository,
                               ContentBeanFactory contentBeanFactory,
                               ApplicationContext appContext,
                               ContentWriter contentWriter,
                               ContentSyncJobJanitor contentSyncJobJanitor
    ) {
        this.contentRepository = contentRepository;
        this.contentBeanFactory = contentBeanFactory;
        this.appContext = appContext;
        this.contentWriter = contentWriter;
        this.contentSyncJobJanitor = contentSyncJobJanitor;
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
        if ((contentSync != null) && contentSync.isActive()) {
            startJobThread(contentSync);
        }
    }

    private void startJobThread(ContentSync contentSync) {
        ContentSyncJob contentSyncJob = getContentSyncJob(contentSync);
        contentSyncJobJanitor.execute(contentSyncJob);
    }

    private ContentSyncJob getContentSyncJob(ContentSync contentSync) {
        String type = contentSync.getType();
        LOG.debug("Try to create job bean: {}", type);
        return (ContentSyncJob) appContext.getBean(type, contentSync, contentWriter);
    }

    @PostConstruct
    public void afterPropertiesSet() {
        if (contentRepository.isContentManagementServer()) {
            this.executeInitialContentQuery();
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
}
