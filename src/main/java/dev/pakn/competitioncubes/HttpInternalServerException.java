package dev.pakn.competitioncubes;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR, reason = "Not Found")
public class HttpInternalServerException extends RuntimeException {
    public HttpInternalServerException() {
        super();
    }
}
