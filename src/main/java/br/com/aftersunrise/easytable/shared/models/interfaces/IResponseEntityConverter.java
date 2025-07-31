package br.com.aftersunrise.easytable.shared.models.interfaces;

import br.com.aftersunrise.easytable.shared.handlers.HandlerResponseWithResult;
import org.springframework.http.ResponseEntity;

public interface IResponseEntityConverter {
    <T> ResponseEntity<T> convert(HandlerResponseWithResult<T> response, boolean withContentOnSuccess);
}