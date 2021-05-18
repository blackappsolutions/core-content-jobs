package de.bas.content.beans;

import com.coremedia.blueprint.common.contentbeans.CMObject;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.struct.Struct;

import java.util.Calendar;
import java.util.List;
import java.util.Optional;

/**
 * Generated interface for beans of document type "ContentJob".
 */
public interface ContentJob extends CMObject {

    /*
     * DEVELOPER NOTE
     * Change the methods to narrow the public interface
     * of the {@link de.bas.content.beans.ContentJobImpl} implementation bean.
     */

    String LOCAL_SETTINGS = "localSettings";
    String SOURCE_CONTENT = "sourceContent";
    String START_AT = "start-at";
    String REPEAT_EVERY = "repeat-every";
    String ACTIVE = "active";
    String LAST_RUN = "lastRun";
    String LAST_RUN_SUCCESSFUL = "lastRunSuccessful";
    String LOG_OUTPUT = "logOutput";

    /**
     * {@link com.coremedia.cap.content.ContentType#getName() Name of the ContentType} 'ContentJob'
     */
    String CONTENTTYPE_CONTENTJOB = "ContentJob";

    /**
     * Returns the value of the document property "localSettings"
     *
     * @return the value
     */
    Struct getLocalSettings();

    /**
     * Returns the value of the document property "lastRun"
     *
     * @return the value
     */
    Calendar getLastRun();

    /**
     * Returns the value of the document property "sourceContent"
     *
     * @return the value
     */
    List<? extends CMObject> getSourceContent();

    /**
     * Returns the value of the document property "active"
     *
     * @return the value
     */
    int getActive();

    /**
     * Returns the value of the document property "lastRunSuccessful"
     *
     * @return the value
     */
    int getLastRunSuccessful();

    /**
     * Returns the value of the document property "startAt"
     *
     * @return the value
     */
    Calendar getStartAt();

    ////////////////////////////////////////////////////
    // Added after the initial contentBean generator run
    //
    Content getContentToSync();

    String getType();

    boolean isActive();

    boolean recursive();

    String getExportStorageURL();

    RepeatEvery getRepetition();

    String getRSS_URL();

    String getZipUrl();

    Optional<String> getZipDirectory();

    String getS3BucketRegion();

    Boolean getS3BucketCleanupDryRun();
}
