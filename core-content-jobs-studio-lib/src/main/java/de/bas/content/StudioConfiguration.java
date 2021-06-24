package de.bas.content;

import com.coremedia.cap.content.ContentType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Markus Schwarz
 */
@Configuration
public class StudioConfiguration {
    @Bean
    public ContentJobWriteInterceptor contentJobWriteInterceptor(@Value("ContentJob") ContentType contentType) {
        ContentJobWriteInterceptor contentJobWriteInterceptor = new ContentJobWriteInterceptor();
        contentJobWriteInterceptor.setType(contentType);
        contentJobWriteInterceptor.setInterceptingSubtypes(true);
        return contentJobWriteInterceptor;
    }

}

