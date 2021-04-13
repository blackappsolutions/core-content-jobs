package de.bas.contentsync.jobs;

import de.bas.contentsync.beans.ContentSync;
import de.bas.contentsync.cae.ContentWriter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

/**
 * @author Markus Schwarz
 */
@Slf4j
public abstract class ContentSyncJob implements Callable<ContentSync> {

    protected final ContentSync contentSync;
    protected final ContentWriter contentWriter;

    public ContentSyncJob(ContentSync contentSync, ContentWriter contentWriter) {
        this.contentSync = contentSync;
        this.contentWriter = contentWriter;
    }

    public ContentSync call() throws Exception {
        contentWriter.startSync(contentSync.getContent().getId());
        boolean successfulRun;
        try {
            doTheSync();
            successfulRun = true;
        } catch (Exception e) {
            log.error("Error while syncing {}", contentSync.getContentToSync(), e);
            successfulRun = false;
        }
        return contentWriter.finishSync(contentSync.getContent().getId(), successfulRun);
    }

    abstract void doTheSync() throws Exception;

    public ContentSync getContentSync() {
        return contentSync;
    }
}


