package de.bas.contentsync.jobs;

import com.coremedia.cap.undoc.server.importexport.base.importer.ServerXmlImport;
import de.bas.contentsync.beans.ContentSync;
import de.bas.contentsync.cae.ContentWriter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Markus Schwarz
 */
@Slf4j
public class ImportXMLJob extends ContentSyncJob {
    public ImportXMLJob(ContentSync contentSync, ContentWriter contentWriter) {
        super(contentSync, contentWriter);
    }

    @Override
    protected void doTheSync() throws Exception {
        ServerXmlImport importer = new ServerXmlImport(
            log,
            contentWriter.getContentRepository().getConnection(),
            null,   // SiteService
            contentSync.recursive(),
            true,  // halt on error
            true,  // validateXml
            false, // skipEntities
            true   // skipUuids
        );
        importer.setZip(contentSync.getExportStorageURL());
        if (contentSync.getZipDirectory().isPresent()) {
            importer.setZipDirectory(contentSync.getZipDirectory().get());
        }
        importer.setThreads(4);
        importer.run();
        importer.
    }
}
