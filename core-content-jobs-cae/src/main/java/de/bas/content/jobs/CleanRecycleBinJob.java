package de.bas.content.jobs;

import com.coremedia.cotopaxi.util.CleanRecycleBin;
import de.bas.content.beans.ContentJob;
import de.bas.content.engine.ContentWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Adaptation of https://documentation.coremedia.com/cmcc-10/artifacts/2010/webhelp/contentserver-en/content/ArchiveCleaner.html
 *
 * Remove all content items from the recycle bin that are not referenced by any other content item outside the recycle bin.
 *
 * @author Markus Schwarz
 */
@Slf4j
@Scope("prototype")
@Component("cleanRecycleBin")
public class CleanRecycleBinJob extends AbstractContentJob{
    public CleanRecycleBinJob(ContentJob contentJob, ContentWriter contentWriter) {
        super(contentJob, contentWriter);
    }

    @Override
    void doTheJob() throws Exception {
        CleanRecycleBin cleanRecycleBin = new CleanRecycleBin() {
            @Override
            public int run(String[] args) {
                super.init();
                int exitCode = super.parseCommandLine(args);
                if (0 != exitCode) {
                    return exitCode;
                }
                connection = contentWriter.getContentRepository().getConnection();
                try {
                    super.run();
                    return 0;
                }
                catch (Exception e) {
                    return 1;
                }
            }
        };
        listAppender = getLoggingEventListAppender("stdout.de.bas.contentsync.jobs.CleanRecycleBinJob$1");
        cleanRecycleBin.run(getArguments());
    }

    private String[] getArguments() {
        List<String> arguments = new ArrayList<>(dummyConnectionArguments);
        String[] cleanRecycleBinArguments = contentJob.getCleanRecycleBinArguments().split(" ");
        arguments.addAll(Arrays.asList(cleanRecycleBinArguments));
        return arguments.toArray(new String[0]);
    }

    protected static final String DUMMY_VALUE = "'connection' below will be used (but parameters are mandatory)";
    protected List<String> dummyConnectionArguments = Arrays.asList(
        "-url", DUMMY_VALUE, "-u", DUMMY_VALUE, "-p", DUMMY_VALUE
    );

}
