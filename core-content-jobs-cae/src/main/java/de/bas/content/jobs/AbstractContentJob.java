package de.bas.content.jobs;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.coremedia.cap.common.CapConnection;
import com.coremedia.cap.common.CapSession;
import de.bas.content.beans.ContentJob;
import de.bas.content.engine.ContentWriter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.util.concurrent.Callable;
import java.util.stream.Collectors;

/**
 * @author Markus Schwarz
 */
@Slf4j
public abstract class AbstractContentJob implements Callable<ContentJob> {

    protected ListAppender<ILoggingEvent> listAppender;
    protected final ContentJob contentJob;
    protected final ContentWriter contentWriter;

    public AbstractContentJob(ContentJob contentJob, ContentWriter contentWriter) {
        this.contentJob = contentJob;
        this.contentWriter = contentWriter;
    }

    abstract void doTheJob() throws Exception;

    public ContentJob call() throws Exception {
        String thisContentJobsID = contentJob.getContent().getId();
        // As we have 'NOT isDeleted AND NOT isCheckedOut' in our initial ContentJob-query and
        // we react on contentCheckedIn() in the ContentJobListener a NullPointerException can not occur here!
        String lastVersionEditor = contentJob.getContent().getCheckedInVersion().getEditor().getName();
        boolean successfulRun;
        String executionProtocol = null;
        try {
            log.info("Checking out content job {} as content-jobs-system-user.", thisContentJobsID);
            contentWriter.startJob(thisContentJobsID);
            log.info("About to start content job {} on behalf of user {}", thisContentJobsID, lastVersionEditor);
            contentWriter.switchToUser(lastVersionEditor, contentWriter.getDomain());
            doTheJob();
            successfulRun = true;
            log.info("Finished content job {} on behalf of user {}", thisContentJobsID, lastVersionEditor);
        } catch (Exception e) {
            log.error("Error while syncing {}", thisContentJobsID, e);
            if (listAppender != null) {
                listAppender.addError("Job run failed.", e);
            }
            successfulRun = false;
        }
        if (listAppender != null) {
            executionProtocol = getProtocol();
        }
        log.info("Checking in job resource {} as content-jobs-system-user.", thisContentJobsID);
        return contentWriter.finishJob(thisContentJobsID, successfulRun, executionProtocol);
    }

    public void cancel() {
        contentWriter.startJob(contentJob.getContent().getId());
        contentWriter.finishJob(contentJob.getContent().getId(), false, "CANCELED");
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
    public ContentJob getContentJobBean() {
        return contentJob;
    }
}


