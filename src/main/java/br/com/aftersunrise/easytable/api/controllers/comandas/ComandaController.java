package br.com.aftersunrise.easytable.api.controllers.comandas;

import br.com.aftersunrise.easytable.borders.dtos.responses.FechamentoResponse;
import br.com.aftersunrise.easytable.borders.handlers.IFechamentoComandaHandler;
import br.com.aftersunrise.easytable.shared.models.interfaces.IResponseEntityConverter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/comandas/v1")
@RequiredArgsConstructor
@Tag(name = "Comandas", description = "Operações relacionadas a comandas.")
public class ComandaController {
    private final IFechamentoComandaHandler fechamentoHandler;
    private final IResponseEntityConverter responseEntityConverter;

    @PostMapping("/{codigoQR}/fechar")
    public CompletableFuture<ResponseEntity<FechamentoResponse>> fecharComanda(
            @PathVariable String codigoQR) {
        return fechamentoHandler.execute(codigoQR)
                .thenApplyAsync(response -> responseEntityConverter.convert(response, true));
    }
}
