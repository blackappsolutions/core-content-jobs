package de.bas.content.jobs;

import de.bas.content.beans.ContentJob;
import de.bas.content.engine.ContentWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Adaptation of https://documentation.coremedia.com/cmcc-10/artifacts/2010/webhelp/contentserver-en/content/VersionCollector.html
 *
 * @author Markus Schwarz
 */
@Slf4j
@Scope("prototype")
@Component("cleanVersions")
public class CleanVersionsJob extends AbstractContentJob{
    public CleanVersionsJob(ContentJob contentJob, ContentWriter contentWriter) {
        super(contentJob, contentWriter);
    }

    @Override
    void doTheJob() throws Exception {
        // com.coremedia.cotopaxi.util.CleanVersions.main(new String[0]);
    }
}
