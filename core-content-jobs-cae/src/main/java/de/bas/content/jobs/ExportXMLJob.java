package de.bas.content.jobs;

import com.coremedia.cap.content.Content;
import com.coremedia.cap.undoc.server.importexport.base.exporter.ServerXmlExport;
import com.coremedia.cap.util.CapStructUtil;
import com.coremedia.xml.Markup;
import com.coremedia.xml.MarkupUtil;
import de.bas.content.engine.ContentWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static de.bas.content.beans.ContentJob.SOURCE_CONTENT;
import static de.bas.content.beans.ContentJob.SOURCE_CONTENT_PATHS;


/**
 * Sample Job, which makes use of ServerExporter, which has the ability to zip XML&Blobs and transfer them to one of the
 * following urls:
 * - file:///..     (Local file system)
 * - s3://..        (AWS S3 Bucket)
 * - http(s)://..   (PUT to some REST-API)
 * <p>
 * => Default ist 'file:///temp/' (in a docker environment, this path should be made accessible with a volume mapping.)
 * <p>
 * !!! The URL must be terminated with a final slash ('/') !!!
 * <p>
 * The export-zip will be placed there with the following name-pattern: content-id.zip. E.g. file:///temp/1234.zip
 * <p>
 * If you want to use s3 (e.g. "s3://blackapp-content-sync/"), keep in mind, that you can have only ONE bucket per
 * system, because it is not possible to pass s3-credentials on the url or on any other way to the ServerExporter,
 * except variables in the system environment (see global/deployment/docker/compose/default.yml).
 *
 * @author Markus Schwarz
 */
@Slf4j
@Scope("prototype")
@Component("xmlExport")
public class ExportXMLJob extends AbstractContentJob {

    public ExportXMLJob(de.bas.content.beans.ContentJob contentJob, ContentWriter contentWriter) {
        super(contentJob, contentWriter);
    }

    @Override
    protected void doTheJob() {
        listAppender = getLoggingEventListAppender(ServerXmlExport.class);
        ServerXmlExport serverExporter = new ServerXmlExport(contentJob.getContent().getRepository().getConnection(), null);
        String[] ids = getContentIds();
        serverExporter.setContentIds(ids);
        serverExporter.setRecursive(contentJob.recursive());
        serverExporter.setZip(contentJob.getZipUrl());
        log.info("About to start {} server-export of content-ids {}", contentJob.recursive() ? "recursive" : "", ids);
        serverExporter.init();
        serverExporter.doExport();
        log.info("Finished {} server-export of content-ids {}", contentJob.recursive() ? "recursive" : "", ids);
    }

    private String[] getContentIds() {
        List<String> contentIds = new ArrayList<>();
        List<Content> sourceContents = CapStructUtil.getLinks(contentJob.getLocalSettings(), SOURCE_CONTENT);
        for (Content sourceContent : sourceContents) {
            contentIds.add(sourceContent.getId());
        }
        if (contentIds.isEmpty()) {
            Markup markup = contentJob.getLocalSettings().getMarkup(SOURCE_CONTENT_PATHS);
            if (markup != null) {
                String string = MarkupUtil.asPlainText(markup);
                addSourceContentPaths(contentIds, string);
            }
        }
        return contentIds.toArray(String[]::new);
    }

    void addSourceContentPaths(List<String> contentIds, String string2Parse) {
        if (string2Parse != null && !string2Parse.isEmpty()) {
            for (String line : string2Parse.split("\n")) {
                if (line.isEmpty()) {
                    continue;
                }
                Content child = contentWriter.getContentRepository().getChild(line);
                if (child == null) {
                    log.warn("{} is skipped. Does not exist in the repository.", line);
                    continue;
                }
                contentIds.add(child.getId());
            }
        }
    }
}
