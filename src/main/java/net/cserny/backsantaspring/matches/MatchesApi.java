package net.cserny.backsantaspring.matches;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

public class MatchesApi {

    public interface Routes {

        String MATCHES = "/matches";
        String MATCHES_FIND_MATCH = MATCHES + "/{token}";
    }

    public static class Events {

        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        @Data
        public static class InitEvent {

            private String token;
            private boolean flag;
        }
    }

    public static class Payload {

        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        @Data
        public static class NameTokenPair implements Comparable<NameTokenPair> {
            private String name;
            private String token;

            @Override
            public int compareTo(NameTokenPair other) {
                return name.compareTo(other.name);
            }
        }

        @Data
        @Builder
        @AllArgsConstructor
        @NoArgsConstructor
        public static class TargetWrapper {
            private String target;
        }

        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        @Data
        public static class NamesWrapper {
            private Map<String, List<String>> names;
        }
    }
}
