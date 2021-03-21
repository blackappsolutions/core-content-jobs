package de.bas.contentsync.jobs;

import com.coremedia.cap.undoc.server.importexport.base.exporter.ServerXmlExport;
import de.bas.contentsync.beans.ContentSync;
import de.bas.contentsync.cae.ContentWriter;
import lombok.extern.slf4j.Slf4j;

/**
 * Sample Job, which makes use of ServerExporter, which has the ability to zip XML&Blobs and transfer them to one of the
 * following urls:
 * - file:///..     (Local file system)
 * - s3://..        (AWS S3 Bucket)
 * - http(s)://..   (PUT to some REST-API)
 *
 * => Default ist 'file:///temp/' (in a docker environment, this path should be made accessible with a volume mapping.)
 *
 * !!! The URL must be terminated with a final slash ('/') !!!
 *
 * The export-zip will be placed there with the following name-pattern: content-id.zip. E.g. file:///temp/1234.zip
 *
 * If you want to use s3 (e.g. "s3://blackapp-content-sync/"), keep in mind, that you can have only ONE bucket per
 * system, because it is not possible to pass s3-credentials on the url or on any other way to the ServerExporter,
 * except variables in the system environment (see global/deployment/docker/compose/default.yml).
 *
 * @author Markus Schwarz
 */
@Slf4j
public class ExportXMLJob extends ContentSyncJob {
    public ExportXMLJob(ContentSync contentSync, ContentWriter contentWriter) {
        super(contentSync, contentWriter);
    }

    @Override
    protected void doTheSync() {
        ServerXmlExport serverExporter = new ServerXmlExport(contentSync.getContent().getRepository().getConnection(), null);
        String id = contentSync.getContentToSync().getId();
        serverExporter.setContentIds(id);
        serverExporter.setRecursive(contentSync.recursive());
        serverExporter.setZip(contentSync.getExportStorageURL() + contentSync.getContentId() + ".zip");
        log.info("About to start {} server-export of content-id {}", contentSync.recursive() ? "recursive" : "", id);
        serverExporter.init();
        serverExporter.doExport();
        log.info("Finished {} server-export of content-id {}", contentSync.recursive() ? "recursive" : "", id);
    }
}
