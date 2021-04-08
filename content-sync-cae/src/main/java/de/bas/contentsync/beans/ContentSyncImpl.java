package de.bas.contentsync.beans;

import com.coremedia.blueprint.base.settings.SettingsService;
import com.coremedia.blueprint.common.contentbeans.CMFolderProperties;
import com.coremedia.blueprint.common.contentbeans.CMObject;
import com.coremedia.cap.content.Content;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * Generated extension class for beans of document type "ContentSync".
 */
public class ContentSyncImpl extends ContentSyncBase implements ContentSync {

    private static final Logger LOG = LoggerFactory.getLogger(de.bas.contentsync.beans.ContentSync.class);
    /*
     * DEVELOPER NOTE
     * You are invited to change this class by adding additional methods here.
     * Add them to the interface {@link de.bas.contentsync.beans.ContentSync} to make them public.
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
        return settingsService.setting("sync-type", String.class, this);
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

    public void setSettingsService(SettingsService settingsService) {
        this.settingsService = settingsService;
    }
}
