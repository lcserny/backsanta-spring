package net.cserny.backsantaspring;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import java.net.URI;

@HttpExchange(url = "/download", accept = MediaType.APPLICATION_OCTET_STREAM_VALUE)
public interface FileClient {

    /**
     * Streams a file from the file server without buffering it in memory.
     * The caller MUST close the returned resource's InputStream (try-with-resources)
     * to release the underlying HTTP connection.
     *
     * <p>A {@link URI} parameter is used instead of {@code @PathVariable} on purpose:
     * URI templates ({@code /{path}}) would percent-encode {@code /} in nested paths
     * (and {@code /{*path}} is Spring MVC pattern syntax, not valid here), so the
     * caller builds the relative URI with slashes preserved and it is resolved
     * against the {@code RestClient} base URL.
     *
     * @param uri relative file URI, e.g. {@code /download/movies/some file.mp4}
     *            (already path-encoded, slashes preserved)
     * @return streaming resource backed by the live HTTP response body
     */
    @GetExchange(accept = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    InputStreamResource download(URI uri);
}
