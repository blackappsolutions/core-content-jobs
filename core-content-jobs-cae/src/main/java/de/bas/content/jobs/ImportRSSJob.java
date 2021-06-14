package de.bas.content.jobs;

import com.coremedia.cap.content.Content;
import com.coremedia.xml.MarkupFactory;
import com.rometools.rome.feed.synd.SyndContent;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import de.bas.content.engine.ContentWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Just a showcase for a RSS Importer
 */
@Slf4j
@Scope("prototype")
@Component("rssImport")
public class ImportRSSJob extends AbstractContentJob {

    private static final String DIV_NS = "<div xmlns=\"http://www.coremedia.com/2003/richtext-1.0\" xmlns:xlink=\"http://www.w3.org/1999/xlink\">";

    public ImportRSSJob(de.bas.content.beans.ContentJob contentJob, ContentWriter contentWriter) {
        super(contentJob, contentWriter);
    }

    @Override
    protected void doTheJob() throws Exception {
        log.info("About to start rss import of {}", contentJob.getRSS_URL());
        importRSS(contentJob.getRSS_URL());
    }

    public void importRSS(String rss_url) {
        try {
            URL feedUrl = new URL(rss_url);
            SyndFeedInput input = new SyndFeedInput();
            SyndFeed feed = input.build(new XmlReader(feedUrl));

            Content targetFolder = contentJob.getTargetFolder();
            for (SyndEntry entry : feed.getEntries()) {
                Map<String, Object> properties = new HashMap<>();
                properties.put("title", entry.getTitle());
                String description = getDescription(entry.getDescription());
                properties.put("detailText", MarkupFactory.fromString(DIV_NS + "<p>" + description + "</p></div>"));
                Content cmArticle = contentWriter.getContentRepository().createChild(
                    targetFolder,
                    "RssImport_" + System.currentTimeMillis(),
                    "CMArticle",
                    properties
                );
                cmArticle.checkIn();
            }
        } catch (Exception e) {
            log.error("Error reading rss feed", e);
        }

    }

    private String getDescription(SyndContent description) {
        String type = description.getType();
        if("text/html".equals(type)){
            return description.getValue();
        }
        throw new UnsupportedOperationException("Implement me! RSS description type '" + type + "' is currently not supported.");
    }


}
