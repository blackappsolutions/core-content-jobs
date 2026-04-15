package de.bas.content;

import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.content.ContentType;
import com.coremedia.cms.uapi.config.CapRepositoriesConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/**
 * @author Markus Schwarz
 */
@AutoConfiguration
@Import({CapRepositoriesConfiguration.class})
public class ContentJobStudioServerAutoConfiguration {

    private final ContentType contentType;

    public ContentJobStudioServerAutoConfiguration(ContentRepository contentRepository) {
        contentType = contentRepository.getContentType("ContentJob");
    }

    @Bean
    public ContentJobWriteInterceptor contentJobWriteInterceptor() {
        ContentJobWriteInterceptor contentJobWriteInterceptor = new ContentJobWriteInterceptor();
        contentJobWriteInterceptor.setType(contentType);
        contentJobWriteInterceptor.setInterceptingSubtypes(true);
        return contentJobWriteInterceptor;
    }

    @Bean
    public ContentJobValidator contentJobValidator() {
        return new ContentJobValidator(contentType, false);
    }
}

