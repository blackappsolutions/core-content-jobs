package de.bas.content;

import com.coremedia.cap.common.CapPropertyDescriptor;
import com.coremedia.cap.common.CapStructHelper;
import com.coremedia.cap.struct.Struct;
import com.coremedia.cap.struct.StructBuilder;
import com.coremedia.cap.struct.StructService;
import com.coremedia.rest.cap.intercept.ContentWriteInterceptorBase;
import com.coremedia.rest.cap.intercept.ContentWriteRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import java.util.Map;

/**
 * Sets the preconfigured xmlExportStorageUrl (XMLEXPORT_STORAGEURL) in the struct field export-storage-url
 *
 * See {@link com.coremedia.blueprint.studio.rest.intercept.InterceptorsStudioConfiguration} for inspiration.
 * <p>
 * This is how you can detect changes. E.g.
 * <p>
 * Old: CapStructHelper.getString(request.getEntity().getVersions().get(0).getStruct("localSettings"), "job-type")
 * New: CapStructHelper.getString(((ContentWriteRequestImpl) request).properties.get("localSettings"), "job-type")
 * <p>
 *
 * @author Markus Schwarz
 */
@Slf4j
public class ContentJobWriteInterceptor extends ContentWriteInterceptorBase {

    protected static final String EXPORT_STORAGE_URL = "export-storage-url";
    protected static final String LOCAL_SETTINGS = "localSettings";
    protected static final String JOB_TYPE = "job-type";
    protected static final String XML_EXPORT = "xmlExport";

    @Value("${xml-export.storage-url}")
    private String xmlExportStorageUrl;

    @Override
    public void intercept(ContentWriteRequest request) {
        Map<String, Object> properties = request.getProperties();
        Object localSettings = properties.get(LOCAL_SETTINGS);
        if (localSettings != null) {
            Struct localSettingsStruct = (Struct) localSettings;
            String jobType = CapStructHelper.getString(localSettingsStruct, JOB_TYPE);
            if (XML_EXPORT.equals(jobType)) {
                StructBuilder structBuilder = getStructBuilder(localSettingsStruct, request.getEntity().getRepository().getConnection().getStructService());
                setLocalSettingsExportUrl(xmlExportStorageUrl, localSettingsStruct, structBuilder);
                properties.put(LOCAL_SETTINGS, structBuilder.build());
            }
        }
    }

    private StructBuilder getStructBuilder(Struct localSettings, StructService structService) {
        StructBuilder structBuilder = structService.createStructBuilder();
        // https://documentation.coremedia.com/cmcc-10/artifacts/2104.1/javadoc/common/com/coremedia/cap/struct/StructBuilder.html
        for (CapPropertyDescriptor descriptor : localSettings.getType().getDescriptors()) {
            structBuilder.declare(descriptor, localSettings.get(descriptor.getName()));
        }
        return structBuilder;
    }

    private void setLocalSettingsExportUrl(String newSetting, Struct localSettings, StructBuilder structBuilder) {
        if ((localSettings.get(EXPORT_STORAGE_URL) == null) && (structBuilder.getDescriptor(EXPORT_STORAGE_URL) == null)) {
            structBuilder.declareString(EXPORT_STORAGE_URL, Integer.MAX_VALUE, newSetting);
        } else {
            structBuilder.set(EXPORT_STORAGE_URL, newSetting);
        }
    }

}
