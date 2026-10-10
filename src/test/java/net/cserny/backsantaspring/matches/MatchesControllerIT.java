package net.cserny.backsantaspring.matches;

import net.cserny.backsantaspring.Api.ApplicationError;
import net.cserny.backsantaspring.TestConfiguration;
import net.cserny.backsantaspring.matches.MatchesApi.Payload.NameTokenPair;
import net.cserny.backsantaspring.matches.MatchesApi.Payload.TargetWrapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.util.List;

import static net.cserny.backsantaspring.matches.MatchesApi.Routes.MATCHES;
import static net.cserny.backsantaspring.matches.MatchesApi.Routes.MATCHES_FIND_MATCH;
import static net.cserny.backsantaspring.matches.MatchesFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@AutoConfigureRestTestClient
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {TestConfiguration.class})
public class MatchesControllerIT {

    @Autowired
    private RestTestClient restTestClient;

    @Test
    public void canGenerateLinks() {
        List<NameTokenPair> pairs = restTestClient.post().uri(MATCHES)
                .body(namesWrapper())
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<List<NameTokenPair>>() {})
                .returnResult().getResponseBody();

        assertNotNull(pairs);
        assertEquals(3, pairs.size());

        pairs.sort(NameTokenPair::compareTo); // easier to compare results
        assertThat(pairs.getFirst()).returns("casu", NameTokenPair::getName);
        assertThat(pairs.get(1)).returns("leo", NameTokenPair::getName);
        assertThat(pairs.get(2)).returns("sabina", NameTokenPair::getName);
    }

    @Test
    public void exclusionsCannotExhaustAllNames() {
        ApplicationError error = restTestClient.post().uri(MATCHES)
                .body(exhaustedNamesWrapper())
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(ApplicationError.class)
                .returnResult().getResponseBody();

        assertNotNull(error);
        assertThat(error.getDetail()).contains("contains all names");
    }

    @Test
    public void canFindMatch() {
        NameTokenPair firstGenerated = generateAndGetFirst();

        TargetWrapper wrapper = restTestClient.get().uri(MATCHES_FIND_MATCH, firstGenerated.getToken())
                .exchange()
                .expectStatus().isOk()
                .expectBody(TargetWrapper.class)
                .returnResult().getResponseBody();

        assertNotNull(wrapper);
        assertEquals("sabina", wrapper.getTarget());
    }

    @Test
    public void canFindMatchOnlyOnce() {
        NameTokenPair firstGenerated = generateAndGetFirst();

        restTestClient.get().uri(MATCHES_FIND_MATCH, firstGenerated.getToken())
                .exchange()
                .expectStatus().isOk()
                .expectBody(TargetWrapper.class)
                .returnResult().getResponseBody();

        restTestClient.get().uri(MATCHES_FIND_MATCH, firstGenerated.getToken())
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(ApplicationError.class)
                .returnResult().getResponseBody();
    }

    @Test
    public void whenFindingMatchThrowIfNotFound() {
        ApplicationError error = restTestClient.get().uri(MATCHES_FIND_MATCH, "my-token")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(ApplicationError.class)
                .returnResult().getResponseBody();

        assertNotNull(error);
        assertThat(error.getDetail()).contains("No match found for token");
    }

    @Test
    public void canClearMatches() {
        restTestClient.delete().uri(MATCHES)
                .exchange()
                .expectStatus().isNoContent()
                .expectBody().isEmpty();
    }

    private NameTokenPair generateAndGetFirst() {
        List<NameTokenPair> generated = restTestClient.post().uri(MATCHES)
                .body(simpleNamesWrapper())
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<List<NameTokenPair>>() {})
                .returnResult().getResponseBody();
        assertNotNull(generated);
        generated.sort(NameTokenPair::compareTo);
        return generated.getFirst();
    }
}
