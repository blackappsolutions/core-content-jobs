package de.bas.content;

import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentType;
import com.coremedia.cap.struct.Struct;
import com.coremedia.cap.util.CapStructUtil;
import com.coremedia.rest.cap.validation.AbstractContentTypeValidator;
import com.coremedia.rest.validation.Issues;
import com.coremedia.rest.validation.Severity;
import org.springframework.util.ObjectUtils;

/**
 * @see <a href="https://documentation.coremedia.com/cmcc-12/artifacts/2401/webhelp/studio-developer-en/content/ImplementingValidators.html#d0e14235">Implementing Validators</a>
 *
 * @author Markus Schwarz
 */
public class ContentJobValidator extends AbstractContentTypeValidator {
    // Intentionally not used de.bas.contentsync.beans.ContentSync.* (as long as the contentbeans are
    // not in a separate lib) to avoid including the de.bas.contentsync.engine in a second component.
    protected static final String JOB_TYPE = "job-type";
    protected static final String LOCAL_SETTINGS = "localSettings";

    public ContentJobValidator(ContentType type, Boolean isValidatingSubtypes) {
        super(type, isValidatingSubtypes);
    }

    @Override
    public void validate(Content content, Issues issues) {
        Struct localSettings = content.getStruct(LOCAL_SETTINGS);

        // Validate form fields
        if (localSettings != null) {
            if (!CapStructUtil.hasPropertyDescriptor(localSettings, "job-type") ||
                ObjectUtils.isEmpty(localSettings.getString(JOB_TYPE))) {
                // apps/studio-client/apps/main/core-content-jobs-studio-plugin/src/bundles/FormValidation_properties.ts
                // ---------------------------------------------------------------------------------------------------
                // Validator_content_job_type_not_set_text: "Please choose a job type."
                issues.addIssue(Severity.ERROR, JOB_TYPE, "content_job_type_not_set");
            }
        }
    }
}
