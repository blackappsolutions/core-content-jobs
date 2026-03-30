package de.bas.content;

import com.coremedia.cap.common.CapObject;
import com.coremedia.cap.common.CapType;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.struct.Struct;
import com.coremedia.rest.cap.validation.CapTypeValidator;
import com.coremedia.rest.validation.Issues;
import com.coremedia.rest.validation.Severity;
import org.springframework.util.ObjectUtils;

/**
 * @author Markus Schwarz
 */
public class ContentJobValidator implements CapTypeValidator {
    // Intentionally not used de.bas.contentsync.beans.ContentSync.* (as long as the contentbeans are
    // not in a separate lib) to avoid including the de.bas.contentsync.engine in a second component.
    protected static final String JOB_TYPE = "job-type";
    protected static final String LOCAL_SETTINGS = "localSettings";
    private final ContentRepository contentRepository;

    public ContentJobValidator(ContentRepository contentRepository) {
        this.contentRepository = contentRepository;
    }

    @Override
    public CapType getType() {
        return contentRepository.getType("ContentJob");
    }

    @Override
    public boolean isValidatingSubtypes() {
        return false;
    }

    @Override
    public void validate(CapObject content, Issues issues) {
        Struct localSettings = content.getStruct(LOCAL_SETTINGS);

        // Validate form fields
        if (localSettings != null) {
            if (ObjectUtils.isEmpty(localSettings.getString(JOB_TYPE))) {
                // content-sync-studio-plugin/src/main/joo/de/bas/contentsync/studio/bundles/FormValidation.properties
                // ---------------------------------------------------------------------------------------------------
                // Validator_content_sync_type_not_set_text=..
                issues.addIssue(Severity.ERROR, JOB_TYPE, "content_job_type_not_set");
            }
        }
    }
}
