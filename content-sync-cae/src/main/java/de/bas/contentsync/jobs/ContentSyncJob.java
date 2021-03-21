package de.bas.contentsync.jobs;

import de.bas.contentsync.beans.ContentSync;
import de.bas.contentsync.cae.ContentWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

/**
 * @author Markus Schwarz
 */
public abstract class ContentSyncJob implements Callable<ContentSync> {

    private static final Logger LOG = LoggerFactory.getLogger(ContentSyncJob.class);
    protected final ContentSync contentSync;
    private final ContentWriter contentWriter;

    public ContentSyncJob(ContentSync contentSync, ContentWriter contentWriter) {
        this.contentSync = contentSync;
        this.contentWriter = contentWriter;
    }

    public ContentSync call() throws Exception {
        boolean successfulRun;
        try {
            doTheSync();
            successfulRun = true;
        } catch (Exception e) {
            LOG.error("Error while syncing {}", contentSync.getContentToSync(), e);
            successfulRun = false;
        }
        contentWriter.finishSync(contentSync.getContent().getId(), successfulRun);
        return contentSync;
    }

    /**
     * Overwrite me. Dummy implementation.
     */
    protected void doTheSync() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
}


