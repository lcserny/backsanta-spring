package net.cserny.backsantaspring.matches;

import net.cserny.backsantaspring.matches.MatchesApi.Payload.NamesWrapper;
import net.cserny.backsantaspring.matches.MatchesApi.Payload.NamesWrapper.NamesWrapperBuilder;

import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;

public class MatchesFixture {

    public static NamesWrapper namesWrapper() {
        return namesWrapperBuilder().build();
    }

    public static NamesWrapper exhaustedNamesWrapper() {
        return namesWrapperBuilder().names(Map.of("leo", List.of("sabina"), "sabina", List.of("leo"))).build();
    }

    public static NamesWrapper simpleNamesWrapper() {
        return namesWrapperBuilder().names(Map.of("leo", emptyList(), "sabina", emptyList())).build();
    }

    public static NamesWrapperBuilder namesWrapperBuilder() {
        return NamesWrapper.builder()
                .names(Map.of(
                        "leo", List.of("casu"),
                        "sabina", List.of("leo"),
                        "casu", emptyList()));
    }
}
