package de.bas.contentsync.cae;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

/**
 * @author Markus Schwarz
 */
public class ContentSyncJob implements Callable<ContentSync> {

    private static final Logger LOG = LoggerFactory.getLogger(ContentSyncJob.class);
    private ContentSync contentSync;

    public ContentSyncJob(ContentSync contentSync) {
        this.contentSync = contentSync;
    }

    // HERE YOU CAN PROVIDE YOUR CONTENT_SYNC-CODE
    //
    public ContentSync call() throws Exception {
        LOG.info("Syncing ...");
        doTheSync();
        LOG.info("... done");
        contentSync.finishSync(true);
        return contentSync;
    }

    private void doTheSync() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
}


