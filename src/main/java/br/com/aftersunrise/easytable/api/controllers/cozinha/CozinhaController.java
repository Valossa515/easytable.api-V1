package br.com.aftersunrise.easytable.api.controllers.cozinha;

import br.com.aftersunrise.easytable.borders.dtos.requests.ListaPedidosRequest;
import br.com.aftersunrise.easytable.borders.dtos.responses.ListaPedidosResponse;
import br.com.aftersunrise.easytable.borders.handlers.IListPedidosHandler;
import br.com.aftersunrise.easytable.shared.models.Message;
import br.com.aftersunrise.easytable.shared.models.interfaces.IResponseEntityConverter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/cozinha/v1")
@RequiredArgsConstructor
@Tag(name = "Cozinha", description = "Operações relacionadas à cozinha.")
public class CozinhaController {

    private final IListPedidosHandler listPedidosHandler;
    private final IResponseEntityConverter responseEntityConverter;

    @Operation(
            summary = "Lista todos os pedidos na cozinha",
            description = "Obtém uma lista de todos os pedidos que estão na cozinha.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Criado com sucesso",
                            content = @Content(schema = @Schema(implementation = ListaPedidosResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Requisição inválida",
                            content = @Content(schema = @Schema(implementation = Message[].class))),
                    @ApiResponse(responseCode = "401", description = "Não autorizado",
                            content = @Content(schema = @Schema(implementation = Message[].class))),
                    @ApiResponse(responseCode = "403", description = "Acesso proibido",
                            content = @Content(schema = @Schema(implementation = Message[].class))),
                    @ApiResponse(responseCode = "500", description = "Erro interno",
                            content = @Content(schema = @Schema(implementation = Message[].class)))
            }
    )
    @GetMapping
    public CompletableFuture<ResponseEntity<ListaPedidosResponse>> listarPedidos() {
        var request = new ListaPedidosRequest();
        return listPedidosHandler.execute(request)
                .thenApplyAsync(response -> responseEntityConverter.convert(response, true));
    }
}