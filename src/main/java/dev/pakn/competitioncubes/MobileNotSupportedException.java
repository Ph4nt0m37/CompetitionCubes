package dev.pakn.competitioncubes;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Mobile Not Supported")
public class MobileNotSupportedException extends RuntimeException {
    public MobileNotSupportedException() {
        super();
    }
}
