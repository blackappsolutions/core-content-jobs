package de.bas.contentsync.cae;

import com.coremedia.cap.Cap;
import com.coremedia.cap.common.CapConnection;
import com.coremedia.cap.common.InvalidLoginException;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Calendar;

import static de.bas.contentsync.beans.ContentSync.*;

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

    public ContentRepository getContentRepository() {
        return contentRepository;
    }

    /**
     * We need to query the contentServer again because otherwise we will run into a caching error
     */
    public void finishSync(String contentId, boolean successful) {
        Content content = contentRepository.getContent(contentId);
        if (content.isCheckedOut()) {
            log.warn("ContentSync {} was checked out while a sync is running. Reverting this changes...", contentId);
            content.revert();
        }
        content.checkOut();
        content.set(ACTIVE, 0);
        content.set(LAST_RUN, Calendar.getInstance());
        content.set(LAST_RUN_SUCCESSFUL, successful ? 1 : 0);
        content.checkIn();
    }

    @PostConstruct
    public void initContentRepository() {
        try {
            CapConnection con = Cap.connect(repoUrl, user, pass);
            log.info("Opened connection for user {}", user);
            contentRepository = con.getContentRepository();
        } catch (InvalidLoginException e) {
            log.info("Can not log in user {} at {}. No worries if this happens on cae-live.", user, repoUrl);
        }
    }
}
