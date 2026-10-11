package net.cserny.backsantaspring;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.time.Duration;

@Configuration
public class FileClientConfig {

    @Bean
    public RestClient fileRestClient(RestClient.Builder builder,
                                     @Value("${file-service.base-url}") String baseUrl) {
        // JdkClientHttpRequestFactory streams request/response bodies instead of
        // buffering them, which is what makes InputStreamResource streaming work
        // for large files. Generous read timeout so big downloads don't get cut off.
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory();
        requestFactory.setReadTimeout(Duration.ofMinutes(30));

        return builder.baseUrl(baseUrl)
                .requestFactory(requestFactory)
                // The default ResourceHttpMessageConverter buffers the whole body and
                // rejects InputStreamResource ("Unsupported resource class"). With
                // supportsReadStreaming=true the response body is handed out as a live
                // InputStream — the caller must close it to release the connection.
                .configureMessageConverters(client -> client
                        .registerDefaults()
                        .configureMessageConvertersList(converters -> {
                            converters.removeIf(c -> c instanceof ResourceHttpMessageConverter);
                            converters.add(new ResourceHttpMessageConverter());
                        }))
                .build();
    }

    @Bean
    public FileClient fileClient(RestClient fileRestClient) {
        RestClientAdapter adapter = RestClientAdapter.create(fileRestClient);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory
                .builderFor(adapter)
                .build();
        return factory.createClient(FileClient.class);
    }
}
