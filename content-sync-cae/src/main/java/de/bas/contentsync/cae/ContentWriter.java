package de.bas.contentsync.cae;

import com.coremedia.cap.Cap;
import com.coremedia.cap.common.Blob;
import com.coremedia.cap.common.CapConnection;
import com.coremedia.cap.common.InvalidLoginException;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.objectserver.beans.ContentBeanFactory;
import com.twelvemonkeys.lang.StringUtil;
import de.bas.contentsync.beans.ContentSync;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.activation.MimeTypeParseException;
import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;

import static de.bas.contentsync.beans.ContentSync.ACTIVE;
import static de.bas.contentsync.beans.ContentSync.LAST_RUN;
import static de.bas.contentsync.beans.ContentSync.LAST_RUN_SUCCESSFUL;
import static de.bas.contentsync.beans.ContentSync.LOG_OUTPUT;

/**
 * @author Markus Schwarz
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "delivery.preview-mode", havingValue = "true")
public class ContentWriter {

    @Value("${repository.url}")
    private String repoUrl;
    @Value("${content-sync.user}")
    private String user;
    @Value("${content-sync.pass}")
    private String pass;

    /**
     * We use a separate contentServer connection here to write content.
     */
    private ContentRepository contentRepository;
    private final ContentBeanFactory contentBeanFactory;

    public ContentWriter(ContentBeanFactory contentBeanFactory) {
        this.contentBeanFactory = contentBeanFactory;
    }

    public void startSync(String contentId) {
        Content content = getCheckedOutContent(contentId);
        content.set(ACTIVE, 0);
        contentRepository.getConnection().flush(); // saves our change above
    }

    public ContentSync finishSync(String contentId, boolean successful, String executionProtocol) throws MimeTypeParseException {
        Content content = getCheckedOutContent(contentId);
        content.set(LAST_RUN, Calendar.getInstance());
        content.set(LAST_RUN_SUCCESSFUL, successful ? 1 : 0);
        if (executionProtocol != null) {
            Blob blob = contentRepository.getConnection().getBlobService().fromBytes(executionProtocol.getBytes(StandardCharsets.UTF_8), "text/plain");
            content.set(LOG_OUTPUT, blob);
        }
        content.checkIn();
        return contentBeanFactory.createBeanFor(content, ContentSync.class);
    }

    private Content getCheckedOutContent(String contentId) {
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
    public void initContentRepository() {
        if (StringUtil.isEmpty(user) || StringUtil.isEmpty(pass)) {
            throw new RuntimeException(
                "Please provide admin user account in 'content-sync.user'-, 'content-sync.pass'-properties."
            );
        }
        try {
            CapConnection con = Cap.connect(repoUrl, user, pass);
            log.info("Opened connection for user {}", user);
            contentRepository = con.getContentRepository();
        } catch (InvalidLoginException e) {
            log.info("Can not log in user {} at {}.", user, repoUrl);
        }
    }

    public ContentRepository getContentRepository() {
        return contentRepository;
    }

}
