package de.bas.contentsync.jobs;

import com.coremedia.cap.content.ContentRepository;
import de.bas.contentsync.beans.ContentSync;
import de.bas.contentsync.cae.ContentWriter;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.when;

/**
 * @author mschwarz
 */
@RunWith(MockitoJUnitRunner.class)
public class ImportRSSJobTest {

    @Mock
    ContentSync contentSync;

    @Mock
    ContentWriter contentWriter;
    
    @Mock
    ContentRepository repository;

    @Before
    public void setUp() {
        when(contentWriter.getContentRepository()).thenReturn(repository);
    }

    @Test
    @Ignore // integration test
    public void doTheSync() {
        new ImportRSSJob(contentSync, contentWriter).importRSS("https://rss.nytimes.com/services/xml/rss/nyt/Technology.xml");
    }
}
