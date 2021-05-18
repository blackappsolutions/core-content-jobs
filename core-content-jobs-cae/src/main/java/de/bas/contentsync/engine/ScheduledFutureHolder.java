package de.bas.contentsync.engine;

import de.bas.contentsync.jobs.ContentSyncJob;

import java.util.concurrent.ScheduledFuture;

/**
 * @author Markus Schwarz
 */
public class ScheduledFutureHolder {
    ScheduledFuture<?> future;
    ContentSyncJob contentSyncJob;

    public ScheduledFutureHolder(ScheduledFuture<?> future, ContentSyncJob contentSyncJob) {
        this.future = future;
        this.contentSyncJob = contentSyncJob;
    }

    public ContentSyncJob getContentSyncJob() {
        return contentSyncJob;
    }
}
