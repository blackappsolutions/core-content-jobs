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
        LOG.info("Syncing {} ...", contentSync.getFolderToSync());
        doTheSync();
        LOG.info("... done");
        contentWriter.finishSync(contentSync.getContent().getId(), true);
        return contentSync;
    }

    protected void doTheSync() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
}


