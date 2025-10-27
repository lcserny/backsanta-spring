package net.cserny.backsantaspring.matches;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.cserny.backsantaspring.ErrorHandler;
import net.cserny.backsantaspring.matches.MatchesApi.Payload.NameTokenPair;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.*;

@Slf4j
@Service
@AllArgsConstructor
public class MatchesService {

    private final Map<String, String> generatedMatches = Collections.synchronizedMap(new HashMap<>());
    private final SecureRandom random = new SecureRandom();

    public List<NameTokenPair> generateMatches(Map<String, List<String>> names) {
        validateExclusions(names);
        clearGeneratedMatches();

        List<NameTokenPair> matchPairs = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            matchPairs = generateMatchesInternal(names);
            if (matchPairs.size() == names.size()) {
                break;
            }
        }

        if (matchPairs.size() != names.size()) {
            throw new ErrorHandler.ClientException("Could not generate correct matches from input given");
        }

        return matchPairs;
    }

    public String findTarget(String token) {
        if (!generatedMatches.containsKey(token)) {
            throw new ErrorHandler.ClientException("No match found for token %s".formatted(token));
        }
        return generatedMatches.remove(token);
    }

    public void clearGeneratedMatches() {
        generatedMatches.clear();
    }

    private List<NameTokenPair> generateMatchesInternal(Map<String, List<String>> names) {
        List<String> namesTaken = new ArrayList<>();
        List<NameTokenPair> matches = new ArrayList<>();

        for (Map.Entry<String, List<String>> entry : names.entrySet()) {
            String name = entry.getKey();
            List<String> excludes = entry.getValue();

            Set<String> drawPool = new HashSet<>(names.keySet());
            drawPool.remove(name);
            excludes.forEach(drawPool::remove);
            namesTaken.forEach(drawPool::remove);

            if (drawPool.isEmpty()) {
                log.info("For name {} there are no options to draw from\\n", name);
                continue;
            }

            int idx = random.nextInt(drawPool.size());
            String target = new ArrayList<>(drawPool).get(idx);

            namesTaken.add(target);

            String token = UUID.randomUUID().toString();
            generatedMatches.put(token, target);

            matches.add(NameTokenPair.builder().name(name).token(token).build());
        }

        return matches;
    }

    private void validateExclusions(Map<String, List<String>> names) {
        List<String> allNames = names.keySet().stream().sorted().toList();

        for (Map.Entry<String, List<String>> entry : names.entrySet()) {
            List<String> allExcludes = new ArrayList<>(entry.getValue());
            allExcludes.add(entry.getKey());
            Collections.sort(allExcludes);

            if (allNames.equals(allExcludes)) {
                throw new ErrorHandler.ClientException("Exclude list for %s contains all names".formatted(entry.getKey()));
            }
        }
    }
}
