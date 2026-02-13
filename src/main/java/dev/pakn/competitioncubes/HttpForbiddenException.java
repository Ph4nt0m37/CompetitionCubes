package dev.pakn.competitioncubes;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.FORBIDDEN, reason = "Access Denied")
public class HttpForbiddenException extends RuntimeException {
    public HttpForbiddenException() {
        super();
    }
}
