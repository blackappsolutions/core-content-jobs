package de.bas.content.beans;

import com.coremedia.blueprint.base.settings.SettingsService;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.util.CapStructUtil;
import com.twelvemonkeys.lang.StringUtil;

import java.util.Calendar;
import java.util.List;
import java.util.Optional;

/**
 * Generated extension class for beans of document type "ContentSync".
 */
public class ContentJobImpl extends ContentJobBase implements ContentJob {
    /*
     * DEVELOPER NOTE
     * You are invited to change this class by adding additional methods here.
     * Add them to the interface {@link de.bas.content.beans.ContentJob} to make them public.
     */

    private SettingsService settingsService;

    @Override
    public String getType() {
        return settingsService.setting("job-type", String.class, this);
    }

    public boolean isWebTriggerAllowed() {
        return settingsService.settingWithDefault(WEB_TRIGGER_ALLOWED, Boolean.class, false, this);
    }

    public boolean recursive() {
        return settingsService.settingWithDefault("recursive", Boolean.class, false, this);
    }

    @Override
    public String getExportStorageURL() {
        return settingsService.settingWithDefault("export-storage-url", String.class, "file:///temp/", this);
    }

    public String getRSS_URL() {
        return settingsService.settingWithDefault(RSS_IMPORT_URL, String.class, RSS_DEFAULT_FEED, this);
    }

    /**
     * Returns the value of the document property "startAt"
     *
     * @return the value of the document property "startAt"
     */
    public Calendar getStartAt() {
        return settingsService.setting(START_AT, Calendar.class, this);
    }

    @Override
    public RepeatEvery getRepetition() {
        String setting = settingsService.setting(REPEAT_EVERY, String.class, this);
        if (!StringUtil.isEmpty(setting)) {
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
    public String getCleanRecycleBinArguments() {
        return settingsService.settingWithDefault(CLEAN_RECYCLE_BIN_ARGS, String.class, CLEAN_RECYCLE_BIN_DEFAULT_ARGS, this);
    }

    @Override
    public Boolean getS3BucketCleanupDryRun() {
        return settingsService.settingWithDefault(S3_BUCKET_CLEANUP_DRYRUN, Boolean.class, true, this);
    }

    public void setSettingsService(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    @Override
    public boolean getXmlImportHaltOnError() {
        return settingsService.settingWithDefault(XML_IMPORT_HALT_ON_ERROR, Boolean.class, false, this);
    }

    @Override
    public boolean getValidateXml() {
        return settingsService.settingWithDefault(XML_IMPORT_VALIDATE_XML, Boolean.class, false, this);
    }

    @Override
    public boolean getSkipEntities() {
        return settingsService.settingWithDefault(XML_IMPORT_SKIP_ENTITIES, Boolean.class, false, this);
    }

    @Override
    public boolean getSkipUuids() {
        return settingsService.settingWithDefault(XML_IMPORT_SKIP_UUIDS, Boolean.class, true, this);
    }

    @Override
    public Content getTargetFolder() {
        List<Content> sourceContent = CapStructUtil.getLinks(getLocalSettings(), SOURCE_CONTENT);
        if (sourceContent.size() == 1) {
            Content content = sourceContent.get(0);
            if (content.isFolder()) {
                return content;
            }
        }
        throw new RuntimeException("Please supply a folder!");
    }

    public String getCleanVersionsArguments() {
        //ToDo
        return null;
    }
}
