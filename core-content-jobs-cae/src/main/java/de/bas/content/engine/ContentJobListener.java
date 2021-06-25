package de.bas.content.engine;

import com.coremedia.cap.common.CapConnection;
import com.coremedia.cap.common.CapSession;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.content.events.ContentCheckedInEvent;
import com.coremedia.cap.content.events.ContentCreatedEvent;
import com.coremedia.cap.content.events.ContentEvent;
import com.coremedia.cap.content.events.ContentRepositoryListenerBase;
import com.coremedia.cap.content.query.QueryService;
import com.coremedia.cap.struct.StructBuilder;
import com.coremedia.cap.struct.StructService;
import com.coremedia.cap.user.User;
import com.coremedia.cap.util.StructUtil;
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
import java.util.Collections;

import static com.coremedia.blueprint.base.links.UriConstants.Segments.PREFIX_DYNAMIC;
import static com.coremedia.blueprint.base.links.UriConstants.Segments.SEGMENT_ID;
import static de.bas.content.beans.ContentJob.CONTENTTYPE_CONTENTJOB;
import static de.bas.content.beans.ContentJob.SOURCE_CONTENT;

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

    /**
     * Makes sure 'active' is 0 on content copy! Not possible in ContentJobWriteInterceptor.
     */
    @Override
    public void contentCreated(ContentCreatedEvent event) {
        if (isContentJob(event)) {
            ContentRepository repository = contentWriter.getContentRepository();
            CapConnection connection = repository.getConnection();
            Content content = event.getContent();
            User editor = content.getEditor(); // When a checked-out content was copied, this will work.
            if (editor == null) { // Content with a proper checked-in version was copied.
                editor = content.getVersions().get(0).getEditor(); // The new content will have this ONE last version.
            }
            // Make the change on behalf of the user initiated the copy/creation.
            CapSession capSession = connection.setSession(connection.login(editor.getName(), contentWriter.getDomain()));
            Content checkedOutContent = contentWriter.getCheckedOutContent(content.getId());

            setInitialValues(repository, connection, checkedOutContent);

            connection.flush(); // save the change
            connection.setSession(capSession); // Set the session back to the initial one (Studio system-user)
        }
    }

    private void setInitialValues(ContentRepository repository, CapConnection connection, Content checkedOutContent) {
        checkedOutContent.set(ContentJob.ACTIVE, 0);
        initSourceContentPropertyInLocalSettings(repository, connection, checkedOutContent);
    }

    private void initSourceContentPropertyInLocalSettings(ContentRepository repository, CapConnection connection, Content checkedOutContent) {
        StructService structService = connection.getStructService();
        StructBuilder structBuilder = structService.createStructBuilder();
        structBuilder.declareLinks(SOURCE_CONTENT, repository.getContentContentType(), Collections.emptyList());

        checkedOutContent.set(
            ContentJob.LOCAL_SETTINGS,
            StructUtil.mergeStructs(checkedOutContent.getStruct(ContentJob.LOCAL_SETTINGS), structBuilder.build())
        );
    }

    /**
     * This handler executes a ContentJob. It will be called also for deletions.
     * That's why we do the isInProduction check!
     */
    @Override
    public void contentCheckedIn(ContentCheckedInEvent event) {
        if (isContentJob(event) && event.getContent().isInProduction()) {
            handleRawContentJob(event.getContent());
        }
    }

    /**
     * As we get noticed on every content type, we need to check if this event was triggered by a ContentJob resource.
     */
    private boolean isContentJob(ContentEvent event) {
        Content content = event.getContent();
        return CONTENTTYPE_CONTENTJOB.equals(content.getType().getName());
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
