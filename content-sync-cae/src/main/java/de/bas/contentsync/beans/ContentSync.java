package de.bas.contentsync.beans;

import com.coremedia.blueprint.common.contentbeans.CMObject;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.struct.Struct;

import java.util.Calendar;
import java.util.List;
import java.util.Optional;

/**
 * Generated interface for beans of document type "ContentSync".
 */
public interface ContentSync extends CMObject {

    /*
     * DEVELOPER NOTE
     * Change the methods to narrow the public interface
     * of the {@link de.bas.contentsync.beans.ContentSyncImpl} implementation bean.
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
     * {@link com.coremedia.cap.content.ContentType#getName() Name of the ContentType} 'ContentSync'
     */
    public static final String CONTENTTYPE_CONTENTSYNC = "ContentSync";

    /**
     * Returns the value of the document property "localSettings"
     *
     * @return the value
     */
    public Struct getLocalSettings();

    /**
     * Returns the value of the document property "lastRun"
     *
     * @return the value
     */
    public Calendar getLastRun();

    /**
     * Returns the value of the document property "sourceContent"
     *
     * @return the value
     */
    public List<? extends CMObject> getSourceContent();

    /**
     * Returns the value of the document property "active"
     *
     * @return the value
     */
    public int getActive();

    /**
     * Returns the value of the document property "lastRunSuccessful"
     *
     * @return the value
     */
    public int getLastRunSuccessful();

    /**
     * Returns the value of the document property "startAt"
     *
     * @return the value
     */
    public Calendar getStartAt();

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
