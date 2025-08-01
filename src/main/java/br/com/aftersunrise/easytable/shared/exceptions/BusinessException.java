package br.com.aftersunrise.easytable.shared.exceptions;

import br.com.aftersunrise.easytable.shared.models.ErrorMessage;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BusinessException extends RuntimeException {
    private final ErrorMessage errorMessage;
    private final HttpStatus httpStatus;

    public BusinessException(ErrorMessage errorMessage, HttpStatus httpStatus) {
        super(errorMessage.getMessage());
        this.errorMessage = errorMessage;
        this.httpStatus = httpStatus;
    }

    public BusinessException(String code, String message, HttpStatus httpStatus) {
        this(new ErrorMessage(code, message), httpStatus);
    }

}
