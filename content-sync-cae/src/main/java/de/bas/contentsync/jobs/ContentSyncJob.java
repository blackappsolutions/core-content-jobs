package de.bas.contentsync.jobs;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import de.bas.contentsync.beans.ContentSync;
import de.bas.contentsync.engine.ContentWriter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;
import java.util.stream.Collectors;

/**
 * @author Markus Schwarz
 */
@Slf4j
public abstract class ContentSyncJob implements Callable<ContentSync> {

    protected ListAppender<ILoggingEvent> listAppender;
    protected final ContentSync contentSync;
    protected final ContentWriter contentWriter;

    public ContentSyncJob(ContentSync contentSync, ContentWriter contentWriter) {
        this.contentSync = contentSync;
        this.contentWriter = contentWriter;
    }

    abstract void doTheSync() throws Exception;

    public ContentSync call() throws Exception {
        contentWriter.startSync(contentSync.getContent().getId());
        boolean successfulRun;
        String executionProtocol = null;
        try {
            doTheSync();
            successfulRun = true;
        } catch (Exception e) {
            log.error("Error while syncing {}", contentSync.getContentId(), e);
            successfulRun = false;
        }
        if (listAppender != null) {
            executionProtocol = getProtocol();
        }
        return contentWriter.finishSync(contentSync.getContent().getId(), successfulRun, executionProtocol);
    }

    public void cancel() {
        contentWriter.startSync(contentSync.getContent().getId());
        contentWriter.finishSync(contentSync.getContent().getId(), false, "CANCELED");
    }

    protected ListAppender<ILoggingEvent> getLoggingEventListAppender(String name) {
        Logger logger = (Logger) LoggerFactory.getLogger(name);
        return getListAppender(logger);
    }
    
    protected ListAppender<ILoggingEvent> getLoggingEventListAppender(Class<?> clazz) {
        Logger logger = (Logger) LoggerFactory.getLogger(clazz);
        return getListAppender(logger);
    }

    private ListAppender<ILoggingEvent> getListAppender(Logger logger) {
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
        return listAppender;
    }

    private String getProtocol() {
        return listAppender.list.stream().map(
            iLoggingEvent -> iLoggingEvent.getTimeStamp() + ": " + iLoggingEvent.getFormattedMessage()
        ).collect(Collectors.joining("\n"));
    }

    ///////////////////////////////////////////////////////////
    // GETTERS
    //
    public ContentSync getContentSync() {
        return contentSync;
    }
}


