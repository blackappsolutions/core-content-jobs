package de.bas.content.engine;

import com.coremedia.cap.Cap;
import com.coremedia.cap.common.Blob;
import com.coremedia.cap.common.BlobService;
import com.coremedia.cap.common.CapConnection;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import hox.corem.login.Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.activation.MimeTypeParseException;
import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import static de.bas.content.beans.ContentJob.ACTIVE;
import static de.bas.content.beans.ContentJob.LAST_RUN;
import static de.bas.content.beans.ContentJob.LAST_RUN_SUCCESSFUL;
import static de.bas.content.beans.ContentJob.LOG_OUTPUT;

/**
 * @author Markus Schwarz
 */
@Slf4j
@Component
public class ContentWriter {

    protected static final String TEXT_PLAIN = "text/plain";

    @Value("${repository.url}")
    private String repoUrl;

    @Value("${content-jobs.user}")
    private String contentJobsUser;
    @Value("${content-jobs.domain}")
    private String domain;

    /**
     * We use a separate contentServer connection here to "write" content.
     */
    private ContentRepository contentRepository;

    public void startJob(String contentId, boolean repetitive) {
        switchToUser(contentJobsUser, domain);
        Content content = getCheckedOutContent(contentId);
        if (!repetitive) {
            content.set(ACTIVE, 0);
            contentRepository.getConnection().flush(); // saves our change above
        }
    }

    public void switchToUser(String contentJobsUser, String domain) {
        CapConnection connection = getContentRepository().getConnection();
        connection.setSession(connection.login(contentJobsUser, domain));
    }

    public void finishJob(String contentId, boolean successful, String executionProtocol) {
        switchToUser(contentJobsUser, domain);
        Content content = getCheckedOutContent(contentId);
        content.set(LAST_RUN, Calendar.getInstance());
        content.set(LAST_RUN_SUCCESSFUL, successful ? 1 : 0);
        if (executionProtocol != null) {
            BlobService blobService = contentRepository.getConnection().getBlobService();
            try {
                Blob blob = blobService.fromBytes(executionProtocol.getBytes(StandardCharsets.UTF_8), TEXT_PLAIN);
                content.set(LOG_OUTPUT, blob);
            } catch (MimeTypeParseException e) {
                log.error("Can not deal with Mime type {}", TEXT_PLAIN, e);
            }
        }
        content.checkIn();
    }

    public Content getCheckedOutContent(String contentId) {
        // We need to query the contentServer again because otherwise we will run into a caching error
        Content content = contentRepository.getContent(contentId);
        if (content.isCheckedOut()) {
            String editorUserName = content.getEditor().getName();
            if (content.getEditor().equals(contentRepository.getConnection().getSession().getUser())) {
                log.info("ContentSync {} is already checked out by {}. No action required.", contentId, editorUserName);
            } else {
                log.warn("ContentSync {} was checked out by {} in the meantime. Checking in.", contentId, editorUserName);
                content.checkIn(); // saving this version created by some user in the meantime
                content.checkOut();
            }
        } else {
            content.checkOut();
        }
        return content;
    }

    @PostConstruct
    public void initContentRepository() {
        Map<String, String> params = new HashMap<>();
        params.put(Cap.USER, Service.SERVICENAME_STUDIO);
        params.put(Cap.PASSWORD, Service.SERVICENAME_STUDIO);
        params.put(Cap.DOMAIN, "");
        params.put(Cap.CONTENT_SERVER_URL, repoUrl);
        params.put(Cap.USE_WORKFLOW, "false");
        params.put("servicename", Service.SERVICENAME_STUDIO);
        params.put("servicekey", Service.SERVICEKEY_STUDIO);
        CapConnection capConnection = Cap.connect(params);
        log.info("Opened connection for user {}", Service.SERVICENAME_STUDIO);
        contentRepository = capConnection.getContentRepository();
    }

    // Start VisibleForTesting
    public void setContentRepository(ContentRepository contentRepository) {
        this.contentRepository = contentRepository;
    }
    // End VisibleForTesting

    public ContentRepository getContentRepository() {
        return contentRepository;
    }

    public String getDomain() {
        return domain;
    }

    public String getContentJobsUser() {
        return contentJobsUser;
    }
}
