package de.bas.content.jobs;

import com.coremedia.cap.common.CapPropertyDescriptor;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.content.query.QueryService;
import com.coremedia.cap.struct.Struct;
import com.coremedia.cap.struct.StructBuilder;
import com.coremedia.cap.struct.StructService;
import de.bas.content.beans.ContentJob;
import de.bas.content.engine.ContentWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static de.bas.content.beans.ContentJob.LOCAL_SETTINGS;

/**
 * You can remove <LinkListProperty Name="sourceContent" LinkType="CMObject"/> in content-jobs-doctypes.xml
 * after this job was executed
 * You can keep this job as an example of an Unified API code transformation script.
 *
 * @author Markus Schwarz
 */
@Slf4j
@Scope("prototype")
@Component("migrateSourceContentLinklistIntoLocalSettings")
public class SourceContentPropertyMigrationJob extends AbstractContentJob {

    public SourceContentPropertyMigrationJob(ContentJob contentJob, ContentWriter contentWriter) {
        super(contentJob, contentWriter);
    }

    @Override
    void doTheJob() throws Exception {
        QueryService queryService = contentWriter.getContentRepository().getQueryService();
        Collection<Content> allContentJobs = queryService.poseContentQuery("TYPE ContentJob: NOT isDeleted");
        fromPropertyToLocalSettings(allContentJobs);
    }

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
            List<Content> sourceContents = contentJob.getLinks(SOURCE_CONTENT);
            String cjId = contentJob.getId();
            log.info("{}: {} property is filled with {} items", cjId, SOURCE_CONTENT, sourceContents.size());
            List<Content> newSourceContent = new ArrayList<>(sourceContents.size());
            for (Content sourceContent : sourceContents) {
                if (sourceContent.getType().getName().equals("CMFolderProperties")) {
                    Content sourceContentFolder = sourceContent.getParent();
                    newSourceContent.add(sourceContentFolder);
                    log.info("{}: Transforming CMFolderProperties to folder link to {}", cjId, sourceContentFolder);
                } else {
                    newSourceContent.add(sourceContent);
                }
            }

            Content checkedOutContent = contentWriter.getCheckedOutContent(cjId);

            log.info("{}: {} property is about to be nulled and moved into {}", cjId, SOURCE_CONTENT, LOCAL_SETTINGS);
            checkedOutContent.set(SOURCE_CONTENT, null);
            checkedOutContent.set(LOCAL_SETTINGS, getNewLocalSettingsStruct(contentJob, newSourceContent));
            checkedOutContent.checkIn();
        }
    }

    private Struct getNewLocalSettingsStruct(Content contentJob, List<Content> newSourceContent) {
        ContentRepository repository = contentJob.getRepository();
        StructService structService = repository.getConnection().getStructService();
        Struct localSettings = getLocalSettings(contentJob, structService);
        StructBuilder structBuilder = getStructBuilder(localSettings, structService);
        setLocalSettingsSourceContent(newSourceContent, localSettings, structBuilder, repository);
        return structBuilder.build();
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
