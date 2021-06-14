package de.bas.content.migration;

import com.coremedia.cap.common.CapPropertyDescriptor;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.struct.Struct;
import com.coremedia.cap.struct.StructBuilder;
import com.coremedia.cap.struct.StructService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static de.bas.content.beans.ContentJob.LOCAL_SETTINGS;

/**
 * @author Markus Schwarz
 */
@Slf4j
@Component
public class SourceContentPropertyMigration {

    protected static final String SOURCE_CONTENT = "sourceContent";

    public void fromPropertyToLocalSettings(Collection<Content> allContentJobs) {
        if (allContentJobs == null || allContentJobs.size() == 0) {
            log.info("No contentJobs to migrate.");
            return;
        }
        log.info("Found {} potential contentJobs to migrate", allContentJobs.size());
        for (Content contentJob : allContentJobs) {
            if (contentJob.getType().getDescriptor(SOURCE_CONTENT) == null) {
                log.info("Nothing to migrate. No {} property found.", SOURCE_CONTENT);
                break;
            }
            List<Content> sourceContent = contentJob.getLinks(SOURCE_CONTENT);
            log.info("{}: {} property is filled with {} items", contentJob.getId(), SOURCE_CONTENT, sourceContent.size());
            List<Content> newSourceContent = new ArrayList<>(sourceContent.size());
            for (Content sc : sourceContent) {
                if (sc.getType().getName().equals("CMFolderProperties")) {
                    Content parent = contentJob.getParent();
                    newSourceContent.add(parent);
                    log.info("{}: Transforming CMFolderProperties to folder link to {}", contentJob.getId(), parent.getId());
                } else {
                    newSourceContent.add(contentJob);
                }
            }

            if (contentJob.isCheckedOut()) {
                contentJob.checkIn();
            }
            contentJob.checkOut();

            log.info("{}: {} property is about to be nulled", contentJob.getId(), SOURCE_CONTENT);
            contentJob.set(SOURCE_CONTENT, null);

            ContentRepository repository = contentJob.getRepository();
            StructService structService = repository.getConnection().getStructService();
            Struct localSettings = getLocalSettings(contentJob, structService);
            StructBuilder structBuilder = getStructBuilder(localSettings, structService);

            setLocalSettingsSourceContent(newSourceContent, localSettings, structBuilder, repository);
            contentJob.set(LOCAL_SETTINGS, structBuilder.build().toMarkup().toString());
            contentJob.checkIn();
        }
    }

    private void setLocalSettingsSourceContent(List<Content> newSourceContent, Struct localSettings, StructBuilder structBuilder, ContentRepository repository) {
        if ((localSettings.get(SOURCE_CONTENT) == null) && (structBuilder.getDescriptor(SOURCE_CONTENT) == null)) {
            structBuilder.declareLinks(SOURCE_CONTENT, repository.getContentContentType(), newSourceContent);
        } else {
            structBuilder.set(SOURCE_CONTENT, newSourceContent);
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

    private Struct getLocalSettings(Content content, StructService structService) {
        Struct localSettings = content.getStruct(LOCAL_SETTINGS);
        if (localSettings == null) {
            localSettings = structService.emptyStruct();
        }
        return localSettings;
    }

}
