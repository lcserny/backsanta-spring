package net.cserny.backsantaspring.matches;

import com.fasterxml.jackson.core.JsonProcessingException;
import net.cserny.backsantaspring.Api.ApplicationError;
import net.cserny.backsantaspring.TestConfiguration;
import net.cserny.backsantaspring.matches.MatchesApi.Payload.NameTokenPair;
import net.cserny.backsantaspring.matches.MatchesApi.Payload.TargetWrapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static net.cserny.backsantaspring.matches.MatchesApi.Routes.MATCHES;
import static net.cserny.backsantaspring.matches.MatchesApi.Routes.MATCHES_FIND_MATCH;
import static net.cserny.backsantaspring.matches.MatchesFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.web.util.UriComponentsBuilder.fromUriString;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {TestConfiguration.class})
public class MatchesControllerIT {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void canGenerateLinks() {
        ResponseEntity<List<NameTokenPair>> response = restTemplate.exchange(MATCHES, HttpMethod.POST, new HttpEntity<>(namesWrapper()), new ParameterizedTypeReference<>() {});

        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<NameTokenPair> pairs = response.getBody();
        assertNotNull(pairs);
        assertEquals(3, pairs.size());

        pairs.sort(NameTokenPair::compareTo); // easier to compare results
        assertThat(pairs.getFirst()).returns("casu", NameTokenPair::getName);
        assertThat(pairs.get(1)).returns("leo", NameTokenPair::getName);
        assertThat(pairs.get(2)).returns("sabina", NameTokenPair::getName);
    }

    @Test
    public void exclusionsCannotExhaustAllNames() throws JsonProcessingException {
        ResponseEntity<ApplicationError> response = restTemplate.exchange(MATCHES, HttpMethod.POST, new HttpEntity<>(exhaustedNamesWrapper()), new ParameterizedTypeReference<>() {});

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApplicationError error = response.getBody();
        assertNotNull(error);
        assertThat(error.getDetail()).contains("contains all names");
    }

    @Test
    public void canFindMatch() {
        NameTokenPair firstGenerated = generateAndGetFirst();
        String url = fromUriString(MATCHES_FIND_MATCH).buildAndExpand(Map.of("token", firstGenerated.getToken())).toUriString();

        ResponseEntity<TargetWrapper> response = restTemplate.exchange(url, HttpMethod.GET, null, new ParameterizedTypeReference<>() {},
                firstGenerated.getName(), firstGenerated.getToken());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        TargetWrapper wrapper = response.getBody();
        assertNotNull(wrapper);
        assertEquals("sabina", wrapper.getTarget());
    }

    @Test
    public void canFindMatchOnlyOnce() {
        NameTokenPair firstGenerated = generateAndGetFirst();
        String url = fromUriString(MATCHES_FIND_MATCH).buildAndExpand(Map.of("token", firstGenerated.getToken())).toUriString();

        ResponseEntity<String> firstResponse = restTemplate.exchange(url, HttpMethod.GET, null, new ParameterizedTypeReference<>() {},
                firstGenerated.getName(), firstGenerated.getToken());
        assertEquals(HttpStatus.OK, firstResponse.getStatusCode());

        ResponseEntity<ApplicationError> secondResponse = restTemplate.exchange(url, HttpMethod.GET, null, new ParameterizedTypeReference<>() {},
                firstGenerated.getName(), firstGenerated.getToken());
        assertEquals(HttpStatus.BAD_REQUEST, secondResponse.getStatusCode());
    }

    @Test
    public void whenFindingMatchThrowIfNotFound() {
        ResponseEntity<ApplicationError> response = restTemplate.exchange(MATCHES_FIND_MATCH, HttpMethod.GET, null, new ParameterizedTypeReference<>() {},
                "my-from", "my-token");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApplicationError error = response.getBody();
        assertNotNull(error);
        assertThat(error.getDetail()).contains("No match found for token");
    }

    @Test
    public void canClearMatches() {
        ResponseEntity<Void> response = restTemplate.exchange(MATCHES, HttpMethod.DELETE, null, Void.class);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    private NameTokenPair generateAndGetFirst() {
        List<NameTokenPair> generated = restTemplate.exchange(MATCHES, HttpMethod.POST, new HttpEntity<>(simpleNamesWrapper()), new ParameterizedTypeReference<List<NameTokenPair>>() {}).getBody();
        Collections.sort(generated);
        return generated.getFirst();
    }
}
