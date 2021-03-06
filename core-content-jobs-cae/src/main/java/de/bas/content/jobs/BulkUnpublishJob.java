package de.bas.content.jobs;

import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.content.publication.PublicationService;
import de.bas.content.beans.ContentJob;
import de.bas.content.engine.ContentWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * https://documentation.coremedia.com/cmcc-10/artifacts/2104/webhelp/contentserver-en/content/bulkpublish.html
 *
 * @author Markus Schwarz
 */
@Slf4j
@Scope("prototype")
@Component("bulkUnpublish")
public class BulkUnpublishJob extends BulkPublishJob {

    public BulkUnpublishJob(ContentJob contentJob, ContentWriter contentWriter) {
        super(contentJob, contentWriter);
        cliArguments = Arrays.asList(
                "-url", DUMMY_VALUE, "-u", DUMMY_VALUE, "-p", DUMMY_VALUE,
                "--checkin",
                "--approve",
                "--unpublish",
                "--verbose"
            );
    }

    @Override
    void doTheJob() throws Exception {
        super.doTheJob();          // un-publishes everything in the given folder, but not the folder itself
        withDrawGivenFolderPath();  // un-publishes the given folder itself
    }

    private void withDrawGivenFolderPath() {
        ContentRepository contentRepository = contentWriter.getContentRepository();
        PublicationService publicationService = contentRepository.getPublicationService();
        Content givenRootFolder = contentRepository.getRoot().getChild(folderPath);
        publicationService.toBeWithdrawn(givenRootFolder);
        publicationService.approvePlace(givenRootFolder, true);
        publicationService.publish(givenRootFolder);
    }
}
