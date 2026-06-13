package net.cserny.backsantaspring.matches;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.cserny.backsantaspring.TransactionHandler;
import net.cserny.backsantaspring.matches.MatchesApi.Payload.NameTokenPair;
import net.cserny.backsantaspring.matches.MatchesApi.Payload.NamesWrapper;
import net.cserny.backsantaspring.matches.MatchesApi.Payload.TargetWrapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Random;
import java.util.UUID;

import static net.cserny.backsantaspring.matches.MatchesApi.Routes.MATCHES;
import static net.cserny.backsantaspring.matches.MatchesApi.Routes.MATCHES_FIND_MATCH;

@Slf4j
@Validated
@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class MatchesController {

    private final Random random = new Random();

    private final TransactionHandler transactionHandler;
    private final MatchesService matchesService;
    private final MatchRepository matchesRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    @GetMapping(MATCHES)
    public ResponseEntity<Void> trigger() throws InterruptedException {
        log.info("Triggering matches");

        transactionHandler.runInNewTransaction(() -> {
            matchesRepository.save(MatchEntity.builder()
                    .name(UUID.randomUUID().toString())
                    .flag(random.nextBoolean())
                    .build());

            applicationEventPublisher.publishEvent(MatchesApi.Events.InitEvent.builder()
                    .token(UUID.randomUUID().toString())
                    .flag(random.nextBoolean())
                    .build());
        });

        applicationEventPublisher.publishEvent(MatchesApi.Events.InitEvent.builder()
                .token(UUID.randomUUID().toString())
                .flag(random.nextBoolean())
                .build());

        log.info("After triggering matches");
        return ResponseEntity.ok().build();
    }

    @PostMapping(MATCHES)
    public ResponseEntity<List<NameTokenPair>> generateLinks(@RequestBody NamesWrapper names) {
        log.info("Request received to generate links with: {}", names);
        List<NameTokenPair> pairs = matchesService.generateMatches(names.getNames());
        return ResponseEntity.ok(pairs);
    }

    @GetMapping(MATCHES_FIND_MATCH)
    public ResponseEntity<TargetWrapper> findMatch(@PathVariable String token) {
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
