package net.cserny.backsantaspring;

import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import tools.jackson.core.StreamReadFeature;

@org.springframework.boot.test.context.TestConfiguration
public class TestConfiguration {

    @Bean
    public JsonMapperBuilderCustomizer testJsonMapperBuilderCustomizer() {
        return builder -> builder.enable(StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION);
    }

}
