package de.bas.content.engine;

import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.content.events.ContentCheckedInEvent;
import com.coremedia.cap.content.events.ContentRepositoryListenerBase;
import com.coremedia.cap.content.query.QueryService;
import com.coremedia.objectserver.beans.ContentBeanFactory;
import de.bas.content.beans.ContentJob;
import de.bas.content.jobs.AbstractContentJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.PostConstruct;
import java.util.Collection;

import static com.coremedia.blueprint.base.links.UriConstants.Segments.PREFIX_DYNAMIC;
import static com.coremedia.blueprint.base.links.UriConstants.Segments.SEGMENT_ID;
import static de.bas.content.beans.ContentJob.CONTENTTYPE_CONTENTJOB;

/**
 * @author Markus Schwarz
 */
@Slf4j
@Component
@RequestMapping
public class ContentJobListener extends ContentRepositoryListenerBase {
    public static final String EXECUTE_PATTERN = '/' + PREFIX_DYNAMIC + "/content-jobs/execute" + "/{" + SEGMENT_ID + '}';

    @Value("${initial.query}")
    private String initialQuery;

    private final ContentRepository contentRepository;
    private final ContentBeanFactory contentBeanFactory;
    private final ApplicationContext appContext;
    private final ContentWriter contentWriter;
    private final ContentJobJanitor contentJobJanitor;

    public ContentJobListener(@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection") ContentRepository contentRepository,
                              ContentBeanFactory contentBeanFactory,
                              ApplicationContext appContext,
                              ContentWriter contentWriter,
                              ContentJobJanitor contentJobJanitor
    ) {
        this.contentRepository = contentRepository;
        this.contentBeanFactory = contentBeanFactory;
        this.appContext = appContext;
        this.contentWriter = contentWriter;
        this.contentJobJanitor = contentJobJanitor;
    }

    @Override
    public void contentCheckedIn(ContentCheckedInEvent event) {
        Content content = event.getContent();
        if (CONTENTTYPE_CONTENTJOB.equals(content.getType().getName())) {
            handleRawContentJob(content);
        }
    }

    private void handleRawContentJob(Content content) {
        ContentJob contentJob = contentBeanFactory.createBeanFor(content, ContentJob.class);
        handleContentJob(contentJob);
    }

    @GetMapping(value = EXECUTE_PATTERN)
    public Object handleExecuteRequest(@PathVariable(SEGMENT_ID) ContentJob contentJob) {
        handleContentJob(contentJob);
        return "Job started";
    }

    private void handleContentJob(ContentJob contentJob) {
        if ((contentJob != null) && contentJob.isActive()) {
            startJobThread(contentJob);
        }
    }

    private void startJobThread(ContentJob contentJob) {
        AbstractContentJob abstractContentJob = getContentJob(contentJob);
        contentJobJanitor.execute(abstractContentJob);
    }

    private AbstractContentJob getContentJob(ContentJob contentJob) {
        String type = contentJob.getType();
        log.debug("Try to create job bean: {}", type);
        return (AbstractContentJob) appContext.getBean(type, contentJob, contentWriter);
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
        Collection<Content> activeContentJobs = queryService.poseContentQuery(initialQuery);
        log.info("Found {} active ContentJob resources", activeContentJobs.size());
        for (Content content : activeContentJobs) {
            handleRawContentJob(content);
        }
    }
}
