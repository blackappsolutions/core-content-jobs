package de.bas.content.engine;

import com.coremedia.cap.Cap;
import com.coremedia.cap.common.Blob;
import com.coremedia.cap.common.BlobService;
import com.coremedia.cap.common.CapConnection;
import com.coremedia.cap.common.InvalidLoginException;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.objectserver.beans.ContentBeanFactory;
import com.twelvemonkeys.lang.StringUtil;
import de.bas.content.beans.ContentJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.activation.MimeTypeParseException;
import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;

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
    private String user;
    @Value("${content-jobs.pass}")
    private String pass;
    @Value("${content-jobs.domain}")
    private String domain;

    /**
     * We use a separate contentServer connection here to "write" content.
     */
    private ContentRepository contentRepository;
    private final ContentBeanFactory contentBeanFactory;

    public ContentWriter(ContentBeanFactory contentBeanFactory) {
        this.contentBeanFactory = contentBeanFactory;
    }

    public void startJob(String contentId) {
        Content content = getCheckedOutContent(contentId);
        content.set(ACTIVE, 0);
        contentRepository.getConnection().flush(); // saves our change above
    }

    public ContentJob finishJob(String contentId, boolean successful, String executionProtocol) {
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
        return contentBeanFactory.createBeanFor(content, ContentJob.class);
    }

    public Content getCheckedOutContent(String contentId) {
        // We need to query the contentServer again because otherwise we will run into a caching error
        Content content = contentRepository.getContent(contentId);
        if (content.isCheckedOut()) {
            if (content.getEditor().equals(contentRepository.getConnection().getSession().getUser())) {
                log.info("ContentSync {} is already checked out by user {}. No action required.", contentId, user);
            } else {
                log.warn("ContentSync {} was checked out by {}. Checking in.", contentId, content.getEditor().toString());
                content.checkIn(); // saving this version created by some user in the meantime
                content.checkOut();
            }
        } else {
            content.checkOut();
        }
        return content;
    }

    @PostConstruct
    public void initContentRepository() throws InvalidLoginException {
        if (StringUtil.isEmpty(user) || StringUtil.isEmpty(pass)) {
            throw new RuntimeException(
                "Please provide admin user account in properties 'content-jobs.user' & 'content-jobs.pass'."
            );
        }
        CapConnection con = Cap.connect(repoUrl, user, "".equals(domain) ? null : domain, pass);
        log.info("Opened connection for user {}", user);
        contentRepository = con.getContentRepository();
        log.info("Connection for user {} established successfully", user);
    }

    public ContentRepository getContentRepository() {
        return contentRepository;
    }

}
