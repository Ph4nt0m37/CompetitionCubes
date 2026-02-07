package dev.pakn.competitioncubes;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Not Found")
public class HttpNotFoundException extends RuntimeException {
    public HttpNotFoundException() {
        super();
    }
}
