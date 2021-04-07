package de.bas.contentsync.jobs;

import com.coremedia.cap.content.Content;
import com.coremedia.xml.MarkupFactory;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;
import de.bas.contentsync.beans.ContentSync;
import de.bas.contentsync.cae.ContentWriter;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class ImportRSSJob extends ContentSyncJob {

    private static final String DIV_NS = "<div xmlns=\"http://www.coremedia.com/2003/richtext-1.0\" xmlns:xlink=\"http://www.w3.org/1999/xlink\">";

    public ImportRSSJob(ContentSync contentSync, ContentWriter contentWriter) {
        super(contentSync, contentWriter);
    }

    @Override
    protected void doTheSync() throws Exception {
        log.info("About to start rss import of {}", contentSync.getRSS_URL());
        importRSS(contentSync.getRSS_URL());
    }

    public void importRSS(String rss_url) {
        try {
            URL feedUrl = new URL(rss_url);
            SyndFeedInput input = new SyndFeedInput();
            SyndFeed feed = input.build(new XmlReader(feedUrl));
            Content targetFolder = contentSync.getContentToSync();
            for (SyndEntry entry : (List<SyndEntry>) feed.getEntries()) {
                Map<String, Object> map = new HashMap<>();
                map.put("title", entry.getTitle());
                map.put("teaserText", MarkupFactory.fromString(DIV_NS + "<p>" + entry.getDescription() + "</p></div>"));
                contentWriter.getContentRepository().createChild(targetFolder, "RssImport_" + System.currentTimeMillis(), "CMArticle", map);
            }
        } catch (Exception e) {
            log.error("Error reading rss feed", e);
        }

    }


}
