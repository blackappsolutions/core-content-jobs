package de.bas.contentsync.jobs;

import com.coremedia.cap.server.importexport.base.exporter.ServerExporter;
import de.bas.contentsync.beans.ContentSync;
import de.bas.contentsync.cae.ContentWriter;

import java.io.File;

/**
 * @author Markus Schwarz
 */
public class ExportXMLJob extends ContentSyncJob{
    public ExportXMLJob(ContentSync contentSync, ContentWriter contentWriter) {
        super(contentSync, contentWriter);
    }

    @Override
    protected void doTheSync() {
        // https://documentation.coremedia.com/cmcc-10/artifacts/2101.2/javadoc/common/com/coremedia/cap/server/importexport/base/exporter/ServerExporter.html
        ServerExporter serverExporter = new ServerExporter(contentSync.getContent().getRepository().getConnection(), null);
        serverExporter.setContentIds(contentSync.getFolderToSync().getId());
        serverExporter.setBaseDir(new File("/tmp"));
        serverExporter.setRecursive(true);
        serverExporter.doExport();
    }
}
