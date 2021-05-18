package de.bas.content.jobs;

import com.coremedia.blueprint.common.contentbeans.CMFolderProperties;
import com.coremedia.blueprint.common.contentbeans.CMObject;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.content.publication.PublicationService;
import com.coremedia.cmdline.ExitCodeException;
import com.coremedia.cotopaxi.util.BulkPublish;
import de.bas.content.engine.ContentWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * https://documentation.coremedia.com/cmcc-10/artifacts/2104/webhelp/contentserver-en/content/bulkpublish.html
 *
 * @author Markus Schwarz
 */
@Slf4j
@Scope("prototype")
@Component("bulkPublish")
public class BulkPublishJob extends AbstractContentJob {

    protected String folderPath;
    protected static final String DUMMY_VALUE = "'connection' below will be used (but parameters are mandatory)";
    protected List<String> cliArguments = Arrays.asList(
        "-url", DUMMY_VALUE, "-u", DUMMY_VALUE, "-p", DUMMY_VALUE,
        "--checkin",
        "--approve",
        "--publish",
        "--verbose"
    );

    public BulkPublishJob(de.bas.content.beans.ContentJob contentJob, ContentWriter contentWriter) {
        super(contentJob, contentWriter);
    }

    @Override
    void doTheJob() throws Exception {
        folderPath = getFolderPath();
        BulkPublish bulkPublish = new BulkPublish() {
            @Override
            public int run(String[] args) {
                super.init();
                int exitCode = super.parseCommandLine(args);
                if (0 != exitCode) {
                    return exitCode;
                }
                connection = contentWriter.getContentRepository().getConnection();
                this.assertThatFolderPathIsPlaceApproved();
                try {
                    super.run();
                    return 0;
                } catch (ExitCodeException e) {
                    return e.getExitCode();
                }
            }

            private void assertThatFolderPathIsPlaceApproved() { // https://documentation.coremedia.com/cmcc-10/artifacts/2104/webhelp/uapi-developer-en/content/PublicationService.html
                ContentRepository contentRepository = connection.getContentRepository();
                Content givenRootFolder = contentRepository.getRoot().getChild(folderPath);
                PublicationService publicationService = contentRepository.getPublicationService();
                if (!publicationService.isPlaceApproved(givenRootFolder)) {
                    publicationService.approvePlace(givenRootFolder);
                }
            }
        };
        listAppender = getLoggingEventListAppender("stdout.de.bas.contentsync.jobs.BulkPublishJob$1");
        bulkPublish.run(getArguments(folderPath).toArray(new String[0]));
    }

    private List<String> getArguments(String folderPath) {
        List<String> arguments = new ArrayList<>(cliArguments);
        arguments.add("-f");
        arguments.add(folderPath);
        return arguments;
    }

    private String getFolderPath() {
        List<? extends CMObject> sourceContent1 = contentJob.getSourceContent();
        if (sourceContent1.size() == 1) {
            CMObject cmObject = sourceContent1.get(0);
            if (cmObject instanceof CMFolderProperties) {
                return cmObject.getContent().getParent().getPath();
            }
        }
        throw new RuntimeException("Please supply a FolderProperties marker for the folder you want to publish");
    }
}
