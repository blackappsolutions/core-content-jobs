package de.bas.contentsync.jobs;

import org.junit.Ignore;
import org.junit.Test;

/**
 * @author mschwarz
 */
public class ImportRSSJobTest {

    @Test
    @Ignore // integration test
    public void doTheSync() {
        new ImportRSSJob(null, null).importRSS("https://rss.nytimes.com/services/xml/rss/nyt/Technology.xml");
    }
}
