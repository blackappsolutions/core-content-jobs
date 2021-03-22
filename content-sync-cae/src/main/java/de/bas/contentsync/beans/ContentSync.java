package de.bas.contentsync.beans;

import com.coremedia.blueprint.common.contentbeans.CMFolderProperties;
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
    String LINKED_SETTINGS = "linkedSettings";
    String SOURCE_FOLDER = "sourceFolder";
    String SOURCE_CONTENT = "sourceContent";
    String START_AT = "startAt";
    String ACTIVE = "active";
    String RETRIES = "retries";
    String LAST_RUN = "lastRun";
    String LAST_RUN_SUCCESSFUL = "lastRunSuccessful";

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
     * Returns the value of the document property "linkedSettings"
     *
     * @return the value
     */
    public List/*<? extends CMSettings>*/ getLinkedSettings();

    /**
     * Returns the value of the document property "retries"
     *
     * @return the value
     */
    public int getRetries();

    /**
     * Returns the value of the document property "lastRun"
     *
     * @return the value
     */
    public Calendar getLastRun();

    /**
     * Returns the value of the document property "sourceFolder"
     *
     * @return the value
     */
    public List<? extends CMFolderProperties> getSourceFolder();

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

    SyncType getType();

    boolean isActive();

    boolean recursive();

    String getExportStorageURL();

    String getZipUrl();

    Optional<String> getZipDirectory();
}
