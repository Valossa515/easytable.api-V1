package br.com.aftersunrise.easytable.shared.handlers;

import br.com.aftersunrise.easytable.shared.models.Message;
import br.com.aftersunrise.easytable.shared.properties.MessageResources;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.slf4j.Logger;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;


public abstract class HandlerBase<TInput, TOutput> implements IHandler<TInput, TOutput> {
    private final Validator validator;
    private final Logger logger;

    public HandlerBase(Logger logger, Validator validator) {
        this.logger = logger;
        this.validator = validator;
    }

    @Override
    public CompletableFuture<HandlerResponseWithResult<TOutput>> execute(TInput request) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (validator != null) {
                    Set<ConstraintViolation<TInput>> violations = validator.validate(request);
                    if (!violations.isEmpty()) {
                        throw new ConstraintViolationException(violations);
                    }
                }

                return doExecute(request).join();
            } catch (ConstraintViolationException e) {
                HandlerResponseWithResult<TOutput> response = new HandlerResponseWithResult<>();
                response.setStatusCode(400);
                response.setMessages(
                        e.getConstraintViolations().stream()
                                .map(v -> new Message(v.getPropertyPath().toString(), v.getMessage()))
                                .toList()
                );
                return response;
            } catch (Exception e) {
                logger.error("Unexpected error: {}", e.getMessage(), e);

                HandlerResponseWithResult<TOutput> response = new HandlerResponseWithResult<>();
                response.setStatusCode(500);
                response.setMessages(List.of(new Message("000", MessageResources.get("error.unexpected"))));
                return response;
            }
        });
    }

    protected abstract CompletableFuture<HandlerResponseWithResult<TOutput>> doExecute(TInput request);

    protected HandlerResponseWithResult<TOutput> notFound(String message) {
        HandlerResponseWithResult<TOutput> response = new HandlerResponseWithResult<>();
        response.setStatusCode(404);
        response.setMessages(List.of(new Message(MessageResources.get("error.not_found_error_code"), message)));
        return response;
    }

    protected HandlerResponseWithResult<TOutput> success(TOutput result) {
        HandlerResponseWithResult<TOutput> response = new HandlerResponseWithResult<>();
        response.setStatusCode(200);
        response.setResult(result);
        return response;
    }

    protected HandlerResponseWithResult<TOutput> created(TOutput result) {
        HandlerResponseWithResult<TOutput> response = new HandlerResponseWithResult<>();
        response.setStatusCode(201);
        response.setResult(result);
        return response;
    }

    protected HandlerResponseWithResult<TOutput> badRequest(String code, String message) {
        HandlerResponseWithResult<TOutput> response = new HandlerResponseWithResult<>();
        response.setStatusCode(400);
        response.setMessages(List.of(new Message(code, message)));
        return response;
    }

    protected HandlerResponseWithResult<TOutput> noContent() {
        HandlerResponseWithResult<TOutput> response = new HandlerResponseWithResult<>();
        response.setStatusCode(204);
        response.setResult(null);
        return response;
    }

    protected HandlerResponseWithResult<TOutput> unauthorized(String code, String message) {
        HandlerResponseWithResult<TOutput> response = new HandlerResponseWithResult<>();
        response.setStatusCode(401);
        response.setMessages(List.of(new Message(code, message)));
        return response;
    }

    protected HandlerResponseWithResult<TOutput> forbidden (String code, String message) {
        HandlerResponseWithResult<TOutput> response = new HandlerResponseWithResult<>();
        response.setStatusCode(403);
        response.setMessages(List.of(new Message(MessageResources.get("forbidden.error_code"), MessageResources.get("forbidden.error_message") )));
        return response;
    }

    protected HandlerResponseWithResult<TOutput> internalServerError(String code, String message) {
        HandlerResponseWithResult<TOutput> response = new HandlerResponseWithResult<>();
        response.setStatusCode(500);
        response.setMessages(List.of(new Message(code, message)));
        return response;
    }
}
