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
    String RSS_DEFAULT_FEED = "https://rss.nytimes.com/services/xml/rss/nyt/Technology.xml";
    String RSS_IMPORT_URL = "rss-import-url";

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // https://documentation.coremedia.com/cmcc-10/artifacts/2010/webhelp/contentserver-en/content/VersionCollector.html
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    String CLEAN_VERSIONS_ARGS = "clean-versions-arguments";
    String CLEAN_VERSIONS_DEFAULT_ARGS = "--noexport";


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // https://documentation.coremedia.com/cmcc-10/artifacts/2010/webhelp/contentserver-en/content/ArchiveCleaner.html
    // --noexport 	No XML files will be generated if this option is set. By default, XML files will be generated.
    // --before     Only remove content items with modification date before or equal to the given date.
    //              Format: yyyyMMddHHmmss.
    //              Default is 30 days from the actual date.
    // --simulate 	Enables simulation mode
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    String CLEAN_RECYCLE_BIN_ARGS = "clean-recycle-bin-arguments";
    String CLEAN_RECYCLE_BIN_DEFAULT_ARGS = "--noexport";
    String S3_BUCKET_CLEANUP_DRYRUN = "s3-bucket-cleanup-dryrun";
    String XML_IMPORT_HALT_ON_ERROR = "xmlImport-haltOnError";
    String XML_IMPORT_VALIDATE_XML = "xmlImport-validateXml";
    String XML_IMPORT_SKIP_ENTITIES = "xmlImport-skipEntities";
    String XML_IMPORT_SKIP_UUIDS = "xmlImport-skipUuids";
    String WEB_TRIGGER_ALLOWED = "web-trigger-allowed";

    /*
     * DEVELOPER NOTE
     * Change the methods to narrow the public interface
     * of the {@link de.bas.content.beans.ContentJobImpl} implementation bean.
     */

    String LOCAL_SETTINGS = "localSettings";
    String SOURCE_CONTENT = "sourceContent";
    String SOURCE_CONTENT_PATHS = "sourceContentPaths";
    String START_AT = "start-at";
    String REPEAT_EVERY = "run-job-every";
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
    String getType();

    boolean isActive();

    boolean recursive();

    String getExportStorageURL();

    RepeatEvery getRepetition();

    String getRSS_URL();

    String getZipUrl();

    Optional<String> getZipDirectory();

    Boolean getS3BucketCleanupDryRun();

    String getCleanRecycleBinArguments();

    String getCleanVersionsArguments();

    boolean getXmlImportHaltOnError();

    boolean getValidateXml();

    boolean getSkipEntities();
    
    boolean getSkipUuids();

    Content getTargetFolder();

    boolean isWebTriggerAllowed();
}
