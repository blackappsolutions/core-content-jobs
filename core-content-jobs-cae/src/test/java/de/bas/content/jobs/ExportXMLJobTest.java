package de.bas.content.jobs;

import com.coremedia.blueprint.testing.ContentTestConfiguration;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.test.xmlrepo.XmlRepoConfiguration;
import com.coremedia.cap.test.xmlrepo.XmlUapiConfig;
import com.coremedia.cms.delivery.configuration.DeliveryConfigurationProperties;
import de.bas.content.engine.ContentWriter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Scope;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.osgi.framework.Constants.SCOPE_SINGLETON;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ExportXMLJobTest.LocalConfig.class)
public class ExportXMLJobTest {

    @Configuration
    @EnableConfigurationProperties({
            DeliveryConfigurationProperties.class
    })
    @Import({XmlRepoConfiguration.class, ContentTestConfiguration.class})
    public static class LocalConfig {
        private static final String CONTENT_REPOSITORY = "classpath:/com/coremedia/testing/contenttest.xml";

        @Bean
        @Scope(SCOPE_SINGLETON)
        public XmlUapiConfig xmlUapiConfig() {
            return new XmlUapiConfig(CONTENT_REPOSITORY);
        }
    }

    @Inject
    private ContentRepository repository;

    @Test
    public void addSourceContentPaths() {
        List<String> contentIds = new ArrayList<>();
        String paths =
            "/Content Test/notSearchable\n" +
            "/Content Test/isSearchable\n" +
            "/Content Test/searchableNotSet";
        ContentWriter contentWriter = new ContentWriter(null);
        contentWriter.setContentRepository(repository);
        new ExportXMLJob(null, contentWriter).addSourceContentPaths(contentIds, paths);
        assertEquals(3, contentIds.size());
    }
}
