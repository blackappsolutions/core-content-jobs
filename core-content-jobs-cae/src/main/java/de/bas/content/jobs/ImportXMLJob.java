package de.bas.content.jobs;

import com.coremedia.cap.undoc.server.importexport.base.exporter.ServerXmlExport;
import com.coremedia.cap.undoc.server.importexport.base.importer.ServerXmlImport;
import de.bas.content.engine.ContentWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Markus Schwarz
 */
@Slf4j
@Scope("prototype")
@Component("xmlImport")
public class ImportXMLJob extends AbstractContentJob {
    public ImportXMLJob(de.bas.content.beans.ContentJob contentJob, ContentWriter contentWriter) {
        super(contentJob, contentWriter);
    }

    @Override
    protected void doTheJob() throws Exception {
        listAppender = getLoggingEventListAppender(ImportXMLJob.class);
        ServerXmlImport importer = new ServerXmlImport(
            log,
            contentWriter.getContentRepository().getConnection(),
            null,   // SiteService
            contentJob.recursive(),
            contentJob.getXmlImportHaltOnError(),
            contentJob.getValidateXml(),
            contentJob.getSkipEntities(),
            contentJob.getSkipUuids()
        );
        importer.setZip(contentJob.getExportStorageURL());
        if (contentJob.getZipDirectory().isPresent()) {
            importer.setZipDirectory(contentJob.getZipDirectory().get());
        }
        importer.setThreads(4);
        importer.run();
    }
}
