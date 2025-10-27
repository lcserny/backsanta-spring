package net.cserny.backsantaspring;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ErrorHandler {

    public static class ClientException extends RuntimeException {
        public ClientException(String message) {
            super(message);
        }
    }

    public static class ServerException extends RuntimeException {
        public ServerException(String message) {
            super(message);
        }
    }

    @ExceptionHandler(ClientException.class)
    public ResponseEntity<Api.ApplicationError> handleClientException(ClientException ex) {
        Api.ApplicationError error = Api.ApplicationError.builder()
                .httpStatus(HttpStatus.BAD_REQUEST.value())
                .type(ex.getClass().getName())
                .detail(ex.getMessage())
                .build();
        return ResponseEntity.status(error.getHttpStatus()).body(error);
    }

    @ExceptionHandler({Exception.class, ServerException.class})
    public ResponseEntity<Api.ApplicationError> handleException(Exception ex) {
        Api.ApplicationError error = Api.ApplicationError.builder()
                .httpStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .type(ex.getClass().getName())
                .detail(ex.getMessage())
                .build();
        return ResponseEntity.status(error.getHttpStatus()).body(error);
    }
}
