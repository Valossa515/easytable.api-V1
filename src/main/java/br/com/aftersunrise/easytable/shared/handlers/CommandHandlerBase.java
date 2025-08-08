package br.com.aftersunrise.easytable.shared.handlers;

import br.com.aftersunrise.easytable.shared.models.Message;
import br.com.aftersunrise.easytable.shared.properties.MessageResources;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;


public abstract class CommandHandlerBase<TCommand extends ICommand<TResponse>, TResponse>
        implements IHandler<TCommand, TResponse> {

    private final Validator validator;
    private final Logger logger;

    public CommandHandlerBase(Logger logger, Validator validator) {
        this.validator = validator;
        this.logger = logger;
    }

    @Override
    public CompletableFuture<HandlerResponseWithResult<TResponse>> execute(TCommand request) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (validator != null) {
                    Set<ConstraintViolation<TCommand>> violations = validator.validate(request);
                    if (!violations.isEmpty()) {
                        throw new ConstraintViolationException(violations);
                    }
                }

                return doExecute(request).join();

            } catch (ConstraintViolationException e) {
                HandlerResponseWithResult<TResponse> response = new HandlerResponseWithResult<>();
                response.setStatusCode(400);
                response.setMessages(
                        e.getConstraintViolations().stream()
                                .map(v -> new Message(v.getPropertyPath().toString(), v.getMessage()))
                                .toList()
                );
                return response;

            } catch (Exception e) {
                logger.error("Unexpected error: {}", e.getMessage(), e);

                HandlerResponseWithResult<TResponse> response = new HandlerResponseWithResult<>();
                response.setStatusCode(500);
                response.setMessages(List.of(new Message("000", MessageResources.get("error.unexpected"))));
                return response;
            }
        });
    }

    protected abstract CompletableFuture<HandlerResponseWithResult<TResponse>> doExecute(TCommand request);

    protected HandlerResponseWithResult<TResponse> notFound(String message, String s) {
        HandlerResponseWithResult<TResponse> response = new HandlerResponseWithResult<>();
        response.setStatusCode(404);
        response.setMessages(List.of(new Message(MessageResources.get("error.not_found_error_code"), message)));
        return response;
    }

    protected HandlerResponseWithResult<TResponse> success(TResponse result) {
        HandlerResponseWithResult<TResponse> response = new HandlerResponseWithResult<>();
        response.setStatusCode(200);
        response.setResult(result);
        return response;
    }

    protected HandlerResponseWithResult<TResponse> badRequest(String code, String message) {
        HandlerResponseWithResult<TResponse> response = new HandlerResponseWithResult<>();
        response.setStatusCode(400);
        response.setMessages(List.of(new Message(code, message)));
        return response;
    }

    protected HandlerResponseWithResult<TResponse> noContent() {
        HandlerResponseWithResult<TResponse> response = new HandlerResponseWithResult<>();
        response.setStatusCode(204);
        response.setResult(null);
        return response;
    }

    protected HandlerResponseWithResult<TResponse> unauthorized(String code, String message) {
        HandlerResponseWithResult<TResponse> response = new HandlerResponseWithResult<>();
        response.setStatusCode(401);
        response.setMessages(List.of(new Message(code, message)));
        return response;
    }

    protected HandlerResponseWithResult<TResponse> forbidden(String code, String message) {
        HandlerResponseWithResult<TResponse> response = new HandlerResponseWithResult<>();
        response.setStatusCode(403);
        response.setMessages(List.of(new Message(MessageResources.get("forbidden.error_code"), MessageResources.get("forbidden.error_message"))));
        return response;
    }

    protected HandlerResponseWithResult<TResponse> internalServerError(String code, String message) {
        HandlerResponseWithResult<TResponse> response = new HandlerResponseWithResult<>();
        response.setStatusCode(500);
        response.setMessages(List.of(new Message(code, message)));
        return response;
    }
}
