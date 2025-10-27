package net.cserny.backsantaspring;

import lombok.*;

public class Api {

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ApplicationError {

        private int httpStatus;
        private String type;
        private String detail;
    }
}
