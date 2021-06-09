package de.bas.content.beans;

import com.coremedia.blueprint.base.settings.SettingsService;
import com.coremedia.blueprint.common.contentbeans.CMFolderProperties;
import com.coremedia.blueprint.common.contentbeans.CMObject;
import com.coremedia.cap.content.Content;
import com.twelvemonkeys.lang.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.List;
import java.util.Optional;

/**
 * Generated extension class for beans of document type "ContentSync".
 */
public class ContentJobImpl extends ContentJobBase implements ContentJob {

    private static final Logger LOG = LoggerFactory.getLogger(ContentJob.class);
    /*
     * DEVELOPER NOTE
     * You are invited to change this class by adding additional methods here.
     * Add them to the interface {@link de.bas.content.beans.ContentJob} to make them public.
     */

    private SettingsService settingsService;

    protected static final String FOLDER_TO_SYNC = "_folderToSync";

    public Content getContentToSync() {
        List<? extends CMObject> sourceFolder = getSourceContent();
        if (!sourceFolder.isEmpty()) {
            CMObject cmObject = sourceFolder.get(0);
            Content content = cmObject.getContent();
            if(cmObject instanceof CMFolderProperties){
                String resourceName = content.getName();
                if (resourceName.equals(FOLDER_TO_SYNC)) {
                    return content.getParent();
                } else {
                    LOG.error("{} is ignored. Must be named {}!", resourceName, FOLDER_TO_SYNC);
                }
            }
            else {
                return content;
            }
        }
        return null;
    }

    @Override
    public String getType() {
        return settingsService.setting("job-type", String.class, this);
    }

    public boolean recursive() {
        return settingsService.settingWithDefault("recursive", Boolean.class, false, this);
    }

    @Override
    public String getExportStorageURL() {
        return settingsService.settingWithDefault("export-storage-url", String.class, "file:///temp/", this);
    }

    public String getRSS_URL() {
        return settingsService.settingWithDefault("rss-import-url", String.class, "https://rss.nytimes.com/services/xml/rss/nyt/Technology.xml", this);
    }

    /**
     * Returns the value of the document property "startAt"
     * @return the value of the document property "startAt"
     */
    public Calendar getStartAt() {
      return settingsService.setting(START_AT, Calendar.class, this);
    }

    @Override
    public RepeatEvery getRepetition() {
        String setting = settingsService.setting(REPEAT_EVERY, String.class, this);
        if(!StringUtil.isEmpty(setting)){
            return RepeatEvery.get(setting);
        }
        return null;
    }

    public boolean isActive() {
        return (getActive() == 1);
    }

    @Override
    public String getZipUrl() {
        return getExportStorageURL() + getContentId() + ".zip";
    }

    @Override
    public Optional<String> getZipDirectory() {
        return settingsService.getSetting("zip-directory", String.class, this);
    }

    @Override
    public Boolean getS3BucketCleanupDryRun() {
        return settingsService.settingWithDefault("s3-bucket-cleanup-dryrun", Boolean.class, true, this);
    }

    public void setSettingsService(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    @Override
    public boolean getXmlImportHaltOnError() {
        return settingsService.settingWithDefault("xmlImport-haltOnError", Boolean.class, false, this);
    }

    @Override
    public boolean getValidateXml() {
        return settingsService.settingWithDefault("xmlImport-validateXml", Boolean.class, false, this);
    }

    @Override
    public boolean getSkipEntities() {
        return settingsService.settingWithDefault("xmlImport-skipEntities", Boolean.class, false, this);
    }

    @Override
    public boolean getSkipUuids() {
        return settingsService.settingWithDefault("xmlImport-skipUuids", Boolean.class, true, this);
    }
}
