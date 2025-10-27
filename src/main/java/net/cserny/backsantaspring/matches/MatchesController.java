package net.cserny.backsantaspring.matches;

import lombok.extern.slf4j.Slf4j;
import net.cserny.backsantaspring.matches.MatchesApi.Payload.NameTokenPair;
import net.cserny.backsantaspring.matches.MatchesApi.Payload.NamesWrapper;
import net.cserny.backsantaspring.matches.MatchesApi.Payload.TargetWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static net.cserny.backsantaspring.matches.MatchesApi.Routes.MATCHES;
import static net.cserny.backsantaspring.matches.MatchesApi.Routes.MATCHES_FIND_MATCH;

@Slf4j
@Validated
@RestController
public class MatchesController {

    private final MatchesService matchesService;

    @Autowired
    public MatchesController(MatchesService matchesService) {
        this.matchesService = matchesService;
    }

    @PostMapping(value = MATCHES, produces =  MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<NameTokenPair>> generateLinks(@RequestBody NamesWrapper names) {
        log.info("Request received to generate links with: {}", names);
        List<NameTokenPair> pairs = matchesService.generateMatches(names.getNames());
        return ResponseEntity.ok(pairs);
    }

    @GetMapping(value = MATCHES_FIND_MATCH, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TargetWrapper> findMatch(@PathVariable("token") String token) {
        log.info("Received request to find match for token {}", token);
        String target = matchesService.findTarget(token);
        return ResponseEntity.ok(TargetWrapper.builder().target(target).build());
    }

    @DeleteMapping(MATCHES)
    public ResponseEntity<Void> clearMatches() {
        log.info("Received request to clear matches");
        matchesService.clearGeneratedMatches();
        return ResponseEntity.noContent().build();
    }
}
