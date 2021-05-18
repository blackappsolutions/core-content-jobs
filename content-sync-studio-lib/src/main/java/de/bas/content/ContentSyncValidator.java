package de.bas.contentsync;

import com.coremedia.cap.content.Content;
import com.coremedia.cap.struct.Struct;
import com.coremedia.rest.cap.validation.ContentTypeValidatorBase;
import com.coremedia.rest.validation.Issues;
import com.coremedia.rest.validation.Severity;
import org.springframework.util.StringUtils;

/**
 * @author Markus Schwarz
 */
public class ContentSyncValidator extends ContentTypeValidatorBase {
    // Intentionally not used de.bas.contentsync.beans.ContentSync.* (as long as the contentbeans are
    // not in a separate lib) to avoid including the de.bas.contentsync.engine in a second component.
    protected static final String SYNC_TYPE = "sync-type";
    protected static final String LOCAL_SETTINGS = "localSettings";

    @Override
    public void validate(Content content, Issues issues) {
        Struct localSettings = content.getStruct(LOCAL_SETTINGS);

        // Validate form fields
        if (localSettings != null) {
            if (StringUtils.isEmpty(localSettings.getString(SYNC_TYPE))) {
                // content-sync-studio-plugin/src/main/joo/de/bas/contentsync/studio/bundles/FormValidation.properties
                // ---------------------------------------------------------------------------------------------------
                // Validator_content_sync_type_not_set_text=..
                issues.addIssue(Severity.ERROR, SYNC_TYPE, "content_sync_type_not_set");
            }
        }
    }
}
