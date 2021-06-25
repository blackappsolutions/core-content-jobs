package de.bas.content.jobs;

import com.coremedia.blueprint.common.contentbeans.CMFolderProperties;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.content.query.QueryService;
import com.coremedia.cap.struct.Struct;
import com.coremedia.cap.struct.StructBuilder;
import com.coremedia.cap.struct.StructService;
import com.coremedia.cap.util.StructUtil;
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
                if (sourceContent.getType().getName().equals(CMFolderProperties.NAME)) {
                    Content sourceContentFolder = sourceContent.getParent();
                    newSourceContent.add(sourceContentFolder);
                    log.info("{}: Transforming CMFolderProperties to folder link to {}", cjId, sourceContentFolder);
                } else {
                    newSourceContent.add(sourceContent);
                }
            }
            if (sourceContents.size() > 0) {
                Content checkedOutContent = contentWriter.getCheckedOutContent(cjId);
                log.info("{}: {} property is about to be nulled and moved into {}", cjId, SOURCE_CONTENT, LOCAL_SETTINGS);
                checkedOutContent.set(SOURCE_CONTENT, null);
                checkedOutContent.set(LOCAL_SETTINGS, getNewLocalSettingsStruct(contentJob, newSourceContent));
                checkedOutContent.checkIn();
            }
        }
    }

    private Struct getNewLocalSettingsStruct(Content contentJob, List<Content> newSourceContent) {
        ContentRepository repository = contentJob.getRepository();
        StructService structService = repository.getConnection().getStructService();
        StructBuilder structBuilder = structService.createStructBuilder();
        structBuilder.declareLinks(SOURCE_CONTENT, repository.getContentContentType(), newSourceContent);
        return StructUtil.mergeStructs(contentJob.getStruct(LOCAL_SETTINGS), structBuilder.build());
    }
}
