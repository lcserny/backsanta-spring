package net.cserny.backsantaspring;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.springframework.web.util.UriUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@RequiredArgsConstructor
@RestController
public class FileController {

    private final FileClient fileClient;

    @Value("${file-service.base-dir:/mnt/d/Downloads}")
    private String baseDir;

    // ---------- SERVER side: serves bytes from disk without loading into memory ----------

    @GetMapping("/download/{*path}")
    public ResponseEntity<InputStreamResource> download(@PathVariable String path) throws IOException {
        Path file = resolve(path);
        log.info("Serving file {}", file);

        if (!Files.isRegularFile(file)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found: " + path);
        }

        long contentLength = Files.size(file);
        InputStream in = Files.newInputStream(file);
        InputStreamResource resource = new InputStreamResource(in);

        return ResponseEntity.ok()
                .contentLength(contentLength)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(file.getFileName().toString(), StandardCharsets.UTF_8)
                        .build().toString())
                .body(resource);
    }

    // ---------- CLIENT side: proxies bytes via FileClient, still without buffering ----------

    @GetMapping("/files/{*path}")
    public ResponseEntity<StreamingResponseBody> stream(@PathVariable String path) {
        log.info("Proxy-streaming file {}", path);

        StreamingResponseBody body = out -> {
            // Execute the HTTP call inside the async streaming thread so the
            // request thread is never blocked and bytes flow straight through.
            try (InputStream in = fileClient.download(downloadUri(path)).getInputStream()) {
                log.info("Downloading file {}", path);
                long copied = in.transferTo(out);
                log.info("Streamed file {} ({} bytes)", path, copied);
            } catch (Exception e) {
                log.error("Streaming failed for {}", path, e);
                throw e instanceof IOException io ? io : new IOException(e);
            }
        };

        Path file = resolve(path);
        ResponseEntity.BodyBuilder builder = ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(file.getFileName().toString(), StandardCharsets.UTF_8)
                        .build().toString());
        try {
            if (Files.isRegularFile(file)) {
                builder.contentLength(Files.size(file));
            }
        } catch (IOException e) {
            log.warn("Could not determine size of {}", file, e);
        }
        return builder.body(body);
    }

    private Path resolve(String path) {
        Path base = Path.of(baseDir).toAbsolutePath().normalize();
        Path file = base.resolve(stripLeadingSlash(path)).normalize();
        if (!file.startsWith(base)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid path: " + path);
        }
        return file;
    }

    private static String stripLeadingSlash(String path) {
        return path.startsWith("/") ? path.substring(1) : path;
    }

    /**
     * Builds the relative file-server URI, preserving {@code /} separators so
     * nested paths route to {@code /download/{*path}}, while encoding everything
     * else (spaces, unicode, ...).
     */
    static URI downloadUri(String path) {
        String encoded = UriUtils.encodePath(stripLeadingSlash(path), StandardCharsets.UTF_8);
        return URI.create("/download/" + encoded);
    }
}
