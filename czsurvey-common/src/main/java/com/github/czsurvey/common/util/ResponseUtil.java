package com.github.czsurvey.common.util;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

/**
 * jhipster <a href="https://github.com/jhipster/jhipster/blob/main/jhipster-framework">ResponseUtil</a>
 * @author YanYu
 */
public class ResponseUtil {

    public static <X> ResponseEntity<X> wrapOrNotFound(Optional<X> maybeResponse) {
        return wrapOrNotFound(maybeResponse, null);
    }

    public static <X> ResponseEntity<X> wrapOrNotFound(Optional<X> maybeResponse, HttpHeaders headers) {
        return maybeResponse.map(response -> ResponseEntity.ok().headers(headers).body(response))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }
}
