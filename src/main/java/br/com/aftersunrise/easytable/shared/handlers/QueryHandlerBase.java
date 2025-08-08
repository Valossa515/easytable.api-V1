package br.com.aftersunrise.easytable.shared.handlers;

import br.com.aftersunrise.easytable.shared.models.Message;
import br.com.aftersunrise.easytable.shared.properties.MessageResources;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@Slf4j
public abstract class QueryHandlerBase<TQuery extends IQuery<TResponse>, TResponse>
        implements IHandler<TQuery, TResponse> {

    private final Validator validator;

    public QueryHandlerBase(Validator validator, MessageResources messageResources) {
        this.validator = validator;
    }

    @Override
    public  CompletableFuture<HandlerResponseWithResult<TResponse>> execute(TQuery request) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                validate(request);
                return doExecute(request).join();
            } catch (ConstraintViolationException e) {
                log.warn("Validação falhou para request: {}", request, e);
                HandlerResponseWithResult<TResponse> response = new HandlerResponseWithResult<>();
                response.setStatusCode(400);
                response.setMessages(
                        e.getConstraintViolations().stream()
                                .map(v -> new Message(v.getPropertyPath().toString(), v.getMessage()))
                                .toList()
                );
                return response;
            } catch (Exception e) {
                log.error("Erro inesperado ao executar query: {}", request, e);
                return internalServerError("ERR500", MessageResources.get("error.unexpected"));
            }
        });
    }

    protected void validate(TQuery query) {
        Set<ConstraintViolation<TQuery>> violations = validator.validate(query);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
    }

    protected abstract CompletableFuture<HandlerResponseWithResult<TResponse>> doExecute(TQuery request);

    protected HandlerResponseWithResult<TResponse> notFound(String message) {
        return buildResponse(404, MessageResources.get("error.not_found_error_code"), message);
    }

    protected HandlerResponseWithResult<TResponse> success(TResponse result) {
        HandlerResponseWithResult<TResponse> response = new HandlerResponseWithResult<>();
        response.setStatusCode(200);
        response.setResult(result);
        return response;
    }

    protected HandlerResponseWithResult<TResponse> badRequest(String code, String message) {
        return buildResponse(400, code, message);
    }

    protected HandlerResponseWithResult<TResponse> noContent() {
        HandlerResponseWithResult<TResponse> response = new HandlerResponseWithResult<>();
        response.setStatusCode(204);
        return response;
    }

    protected HandlerResponseWithResult<TResponse> unauthorized(String code, String message) {
        return buildResponse(401, code, message);
    }

    protected HandlerResponseWithResult<TResponse> forbidden(String code, String message) {
        return buildResponse(
                403,
                MessageResources.get("forbidden.error_code"),
                MessageResources.get("forbidden.error_message")
        );
    }

    protected HandlerResponseWithResult<TResponse> internalServerError(String code, String message) {
        return buildResponse(500, code, message);
    }

    private HandlerResponseWithResult<TResponse> buildResponse(int status, String code, String message) {
        HandlerResponseWithResult<TResponse> response = new HandlerResponseWithResult<>();
        response.setStatusCode(status);
        response.setMessages(List.of(new Message(code, message)));
        return response;
    }
}

