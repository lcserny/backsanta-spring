package net.cserny.backsantaspring;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class FileClientTest {

    /*
    leonardo@DESKTOP-64F25UB:~$ curl -N -o /dev/null -w "ttfb=%{time_starttransfer}s total=%{time_total}s\n" http://localhost:7070/files/hey.mp4
    % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
                                 Dload  Upload   Total   Spent    Left  Speed
    100 1087M  100 1087M    0     0  65.8M      0  0:00:16  0:00:16 --:--:-- 66.5M
    ttfb=0.333389s total=16.504335s
     */

    private final RestClient.Builder restClientBuilder = RestClient.builder()
            .baseUrl("http://localhost:7070")
            .requestFactory(new JdkClientHttpRequestFactory())
            // Same as FileClientConfig: streaming reads for InputStreamResource.
            .configureMessageConverters(client -> client
                    .registerDefaults()
                    .configureMessageConvertersList(converters -> {
                        converters.removeIf(c -> c instanceof ResourceHttpMessageConverter);
                        converters.add(new ResourceHttpMessageConverter());
                    }));

    private final MockRestServiceServer mockServer = MockRestServiceServer.bindTo(restClientBuilder).build();
    private final FileClient fileClient = HttpServiceProxyFactory
            .builderFor(RestClientAdapter.create(restClientBuilder.build()))
            .build()
            .createClient(FileClient.class);

    @Test
    void downloadStreamsNestedPathWithSlashesPreserved() throws Exception {
        byte[] content = "fake-bytes".getBytes(StandardCharsets.UTF_8);
        mockServer.expect(requestTo("http://localhost:7070/download/movies/some%20file.mp4"))
                .andRespond(withSuccess(content, MediaType.APPLICATION_OCTET_STREAM));

        InputStreamResource resource =
                fileClient.download(FileController.downloadUri("/movies/some file.mp4"));
        try (var in = resource.getInputStream()) {
            assertThat(in.readAllBytes()).isEqualTo(content);
        }

        mockServer.verify();
    }
}
