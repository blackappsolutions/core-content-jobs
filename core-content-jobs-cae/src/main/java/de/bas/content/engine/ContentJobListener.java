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
import de.bas.content.beans.ContentJobImpl;
import de.bas.content.jobs.AbstractContentJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.Collections;

import static com.coremedia.blueprint.base.links.UriConstants.Segments.PREFIX_DYNAMIC;
import static com.coremedia.blueprint.base.links.UriConstants.Segments.SEGMENT_ID;
import static de.bas.content.beans.ContentJob.CONTENTTYPE_CONTENTJOB;
import static de.bas.content.beans.ContentJob.RSS_DEFAULT_FEED;
import static de.bas.content.beans.ContentJob.S3_BUCKET_CLEANUP_DRYRUN;
import static de.bas.content.beans.ContentJob.SOURCE_CONTENT;
import static de.bas.content.beans.ContentJob.WEB_TRIGGER_ALLOWED;
import static de.bas.content.beans.ContentJob.XML_IMPORT_HALT_ON_ERROR;
import static de.bas.content.beans.ContentJob.XML_IMPORT_SKIP_ENTITIES;
import static de.bas.content.beans.ContentJob.XML_IMPORT_SKIP_UUIDS;
import static de.bas.content.beans.ContentJob.XML_IMPORT_VALIDATE_XML;

/**
 * @author Markus Schwarz
 */
@Slf4j
@Component
@RequestMapping
public class ContentJobListener extends ContentRepositoryListenerBase {
    protected static final String CONTENT_JOBS_URL_SLUG = "content-jobs";
    protected static final String CONTENT_JOBS_PATTERN = '/' + PREFIX_DYNAMIC + "/" + CONTENT_JOBS_URL_SLUG;
    public static final String EXECUTE_PATTERN = CONTENT_JOBS_PATTERN + "/execute" + "/{" + SEGMENT_ID + '}';
    protected static final String CONTENT_JOBS_FRAMEWORK = "Content-Jobs framework";

    @Value("${initial.query}")
    private String initialQuery;

    private final ContentRepository contentRepository;
    private final ContentBeanFactory contentBeanFactory;
    private final ApplicationContext appContext;
    private final ContentWriter contentWriter;
    private final ContentJobJanitor contentJobJanitor;
    private boolean enabled = true;

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

            setInitialValues(connection, checkedOutContent);

            connection.flush(); // save the change
            connection.setSession(capSession); // Set the session back to the initial one (Studio system-user)
        }
    }

    private void setInitialValues(CapConnection connection, Content checkedOutContent) {
        checkedOutContent.set(ContentJob.ACTIVE, 0);
        initLocalSettings(connection, checkedOutContent);
    }

    private void initLocalSettings(CapConnection connection, Content checkedOutContent) {
        StructService structService = connection.getStructService();
        StructBuilder structBuilder = structService.createStructBuilder();
        structBuilder.declareString(ContentJobImpl.RSS_IMPORT_URL, Integer.MAX_VALUE, RSS_DEFAULT_FEED);
        structBuilder.declareBoolean(S3_BUCKET_CLEANUP_DRYRUN, true);
        structBuilder.declareBoolean(WEB_TRIGGER_ALLOWED, false);
        structBuilder.declareBoolean(XML_IMPORT_HALT_ON_ERROR, false);
        structBuilder.declareBoolean(XML_IMPORT_VALIDATE_XML, false);
        structBuilder.declareBoolean(XML_IMPORT_SKIP_ENTITIES, false);
        structBuilder.declareBoolean(XML_IMPORT_SKIP_UUIDS, true);
        structBuilder.declareLinks(SOURCE_CONTENT, connection.getContentRepository().getContentContentType(), Collections.emptyList());

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
            ContentJob contentJobBean = contentBeanFactory.createBeanFor(event.getContent(), ContentJob.class);
            if (contentJobBean != null) {
                if (contentJobBean.isWebTriggerAllowed()) {
                    // We must skip here, because otherwise our own check-in at the end will lead to an endless loop.
                    // Normally, this is prevented by the isActive()-flag, which is not viable here.
                    log.info(
                        "ContentJob {} was set up to be triggered externally. Skipping this contentCheckedIn()-event.",
                        contentJobBean.getContentId()
                    );
                } else {
                    handleContentJob(contentJobBean);
                }
            }
        }
    }

    /**
     * As we get noticed on every content type, we need to check if this event was triggered by a ContentJob resource.
     */
    private boolean isContentJob(ContentEvent event) {
        Content content = event.getContent();
        return CONTENTTYPE_CONTENTJOB.equals(content.getType().getName());
    }

    @ResponseBody
    @GetMapping(value = EXECUTE_PATTERN)
    public Object handleExecuteRequest(@PathVariable(SEGMENT_ID) ContentJob contentJob) {
        if (contentJob != null) {
            handleContentJob(contentJob);
            return "Job started";
        }
        return "Job was null";
    }

    @ResponseBody
    @GetMapping(value = CONTENT_JOBS_PATTERN)
    public Object handleEnableRequest(@RequestParam("enable") boolean enable) {
        this.enabled = enable;
        return "Set " + CONTENT_JOBS_FRAMEWORK + " " + (enable ? "active" : "on pause");
    }

    private void handleContentJob(ContentJob contentJob) {
        if (this.enabled) {
            if (contentJob.isActive() || contentJob.isWebTriggerAllowed()) {
                startJobThread(contentJob);
            }
            return;
        }
        log.info(
            CONTENT_JOBS_FRAMEWORK + " is paused on this CAE. Maybe a 2nd instance is active for development reasons?"
        );
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
            handleContentJob(contentBeanFactory.createBeanFor(content, ContentJob.class));
        }
    }
}
