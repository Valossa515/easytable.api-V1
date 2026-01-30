package br.com.aftersunrise.easytable.api.controllers.comandas;

import br.com.aftersunrise.easytable.borders.dtos.requests.FechamentoComandaCommand;
import br.com.aftersunrise.easytable.borders.dtos.requests.ReabrirComandaCommand;
import br.com.aftersunrise.easytable.borders.dtos.responses.ComandaResponse;
import br.com.aftersunrise.easytable.borders.dtos.responses.FechamentoResponse;
import br.com.aftersunrise.easytable.borders.handlers.IFechamentoComandaHandler;
import br.com.aftersunrise.easytable.borders.handlers.IReabrirComandaHandler;
import br.com.aftersunrise.easytable.shared.models.interfaces.IResponseEntityConverter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/comandas/v1")
@RequiredArgsConstructor
@Tag(name = "Comandas", description = "Operações relacionadas a comandas.")
public class ComandaController {
    private final IFechamentoComandaHandler fechamentoHandler;
    private final IReabrirComandaHandler reabrirHandler;
    private final IResponseEntityConverter responseEntityConverter;

    @PostMapping("/{codigoQR}/fechar")
    public CompletableFuture<ResponseEntity<FechamentoResponse>> fecharComanda(
            @PathVariable String codigoQR) {
        var command = new FechamentoComandaCommand(codigoQR);
        return fechamentoHandler.execute(command)
                .thenApplyAsync(response -> responseEntityConverter.convert(response, true));
    }

    @PatchMapping("/reabrir/{codigoQR}")
    public CompletableFuture<ResponseEntity<ComandaResponse>> reabrirComanda(
            @PathVariable String codigoQR) {
        var command = new ReabrirComandaCommand(codigoQR);
        return reabrirHandler.execute(command)
                .thenApplyAsync(response -> responseEntityConverter.convert(response, true));
    }
}