package de.bas.contentsync.cae;

import com.coremedia.cap.content.Content;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.Objects;

/**
 * Content-SyncModel (used instead of ContentBeans in order to keep the overhead low).
 */
public class ContentSync {

    private static final Logger LOG = LoggerFactory.getLogger(ContentSync.class);

    // Property names. See content-sync-server/src/main/resources/framework/doctypes/content-sync-doctypes.xml
    public static final String SOURCE_FOLDER = "sourceFolder";
    public static final String START_AT = "startAt";
    public static final String ACTIVE = "active";
    // public static final String RETRIES = "retries";
    public static final String LAST_RUN = "lastRun";
    public static final String LAST_RUN_SUCCESSFUL = "lastRunSuccessful";

    protected static final String FOLDER_TO_SYNC = "_folderToSync";
    private final Content sourceFolderLink;
    private final String contentId;
    private final Calendar startAt;
    private final boolean active;
    // private final int retries;
    private Content contentSync;

    public ContentSync(Content contentSync) {
        sourceFolderLink = contentSync.getLink(SOURCE_FOLDER);
        contentId = contentSync.getId();
        startAt = contentSync.getDate(START_AT);
        active = contentSync.getBoolean(ACTIVE);
        // retries = contentSync.getInteger(RETRIES);
        this.contentSync = contentSync;
    }

    public Content getSourceFolder() {
        if (sourceFolderLink != null) {
            String resourceName = sourceFolderLink.getName();
            if (resourceName.equals(FOLDER_TO_SYNC)) {
                return sourceFolderLink.getParent();
            } else {
                LOG.error("{} is ignored. Must be named {}!", resourceName, FOLDER_TO_SYNC);
            }
        }
        return sourceFolderLink;
    }

    public boolean isActive() {
        if (active) {
            /* ToDo: Future Runs must be scheduled
            if (startAt == null) {
                return true; // start immediately
            } else {
                Calendar now = Calendar.getInstance();
                return startAt.after(now);
            }
            */
            return true;
        }
        return false;
    }

    public String getContentId() {
        return contentId;
    }

    public void finishSync(boolean successful) {
        if (contentSync.isCheckedOut()) {
            LOG.warn("ContentSync {} was checked out while a sync is running. Reverting this changes...", contentId);
            contentSync.revert();
        }
        contentSync.checkOut();
        contentSync.set(ACTIVE, 0);
        contentSync.set(LAST_RUN, Calendar.getInstance());
        contentSync.set(LAST_RUN_SUCCESSFUL, successful ? 1 : 0);
        contentSync.checkIn();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ContentSync contentSync = (ContentSync) o;
        return contentId.equals(contentSync.contentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(contentId);
    }

    @Override
    public String toString() {
        return "ContentSync{" +
            "contentId='" + contentId + '\'' +
            '}';
    }
}
