package de.bas.contentsync.jobs;

import de.bas.contentsync.beans.ContentSync;
import de.bas.contentsync.cae.ContentWriter;

/**
 * @author Markus Schwarz
 */
public class PushToIngestServiceJob extends ContentSyncJob {
    public PushToIngestServiceJob(ContentSync contentSync, ContentWriter contentWriter) {
        super(contentSync, contentWriter);
    }
}
