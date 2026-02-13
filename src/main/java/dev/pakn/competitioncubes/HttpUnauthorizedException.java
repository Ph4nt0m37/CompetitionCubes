package dev.pakn.competitioncubes;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNAUTHORIZED, reason = "Not Logged In")
public class HttpUnauthorizedException extends RuntimeException {
    public HttpUnauthorizedException() {
        super();
    }
}
