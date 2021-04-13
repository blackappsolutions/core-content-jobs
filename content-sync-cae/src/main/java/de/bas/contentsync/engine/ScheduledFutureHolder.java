package de.bas.contentsync.engine;

import de.bas.contentsync.beans.ContentSync;

import java.util.concurrent.ScheduledFuture;

/**
 * @author Markus Schwarz
 */
public class ScheduledFutureHolder {
    ScheduledFuture<?> future;
    ContentSync contentSync;

    public ScheduledFutureHolder(ScheduledFuture<?> future, ContentSync contentSync) {
        this.future = future;
        this.contentSync = contentSync;
    }

    public ContentSync getContentSync() {
        return contentSync;
    }
}
