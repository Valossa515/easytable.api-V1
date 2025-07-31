package br.com.aftersunrise.easytable.shared.handlers;

import java.util.concurrent.CompletableFuture;

public interface IHandler<TInput, TOutput> {
    CompletableFuture<HandlerResponseWithResult<TOutput>> execute(TInput request);
}
