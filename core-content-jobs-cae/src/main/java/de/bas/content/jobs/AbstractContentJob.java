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

    @Value("${content-jobs.domain}")
    private String domain;

    public AbstractContentJob(ContentJob contentJob, ContentWriter contentWriter) {
        this.contentJob = contentJob;
        this.contentWriter = contentWriter;
    }

    abstract void doTheJob() throws Exception;

    public ContentJob call() throws Exception {
        String thisContentJobsID = contentJob.getContent().getId();
        String lastVersionEditor = contentJob.getContent().getCheckedInVersion().getEditor().getName();
        CapConnection connection = contentWriter.getContentRepository().getConnection();
        boolean successfulRun;
        CapSession previousSession = null;
        String executionProtocol = null;
        try {
            log.info("About to start content job on behalf of user {}", lastVersionEditor);
            previousSession = connection.setSession(connection.login(lastVersionEditor, domain));
            contentWriter.startJob(thisContentJobsID);
            doTheJob();
            successfulRun = true;
        } catch (Exception e) {
            log.error("Error while syncing {}", thisContentJobsID, e);
            if (listAppender != null) {
                listAppender.addError("Job run failed.", e);
            }
            successfulRun = false;
        } finally {
            if (previousSession != null) {
                // maybe superfluous because the thread ends anyway,
                // but tells that we are having good habits ;-)
                connection.setSession(previousSession);
            }
        }
        if (listAppender != null) {
            executionProtocol = getProtocol();
        }
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


