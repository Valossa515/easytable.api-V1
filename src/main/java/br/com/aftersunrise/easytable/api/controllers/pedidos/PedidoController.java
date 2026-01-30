package br.com.aftersunrise.easytable.api.controllers.pedidos;

import br.com.aftersunrise.easytable.borders.dtos.requests.CreatePedidoCommand;
import br.com.aftersunrise.easytable.borders.dtos.requests.UpdateStatusPedidoCommand;
import br.com.aftersunrise.easytable.borders.dtos.responses.CreatePedidoResponse;
import br.com.aftersunrise.easytable.borders.dtos.responses.UpdateStatusPedidoResponse;
import br.com.aftersunrise.easytable.borders.handlers.ICreatePedidoHandler;
import br.com.aftersunrise.easytable.borders.handlers.IUpdateStatusPedidoHandler;
import br.com.aftersunrise.easytable.shared.enums.PedidoStatusEvent;
import br.com.aftersunrise.easytable.shared.models.Message;
import br.com.aftersunrise.easytable.shared.models.interfaces.IResponseEntityConverter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/pedidos/v1")
@RequiredArgsConstructor
@Tag(name = "Pedidos", description = "Operações relacionadas a pedidos.")
public class PedidoController {

    private final ICreatePedidoHandler createPedidoHandler;
    private final IUpdateStatusPedidoHandler updateStatusPedidoHandler;
    private final IResponseEntityConverter responseEntityConverter;

    @Operation(
            summary = "Cria um novo pedido",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Criado com sucesso",
                            content = @Content(schema = @Schema(implementation = CreatePedidoResponse.class))),
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
    @PostMapping("/create")
    public CompletableFuture<ResponseEntity<CreatePedidoResponse>> createPedido(
            @RequestBody CreatePedidoCommand request) {
        return createPedidoHandler.execute(request)
                .thenApplyAsync(response -> responseEntityConverter.convert(response, true));
    }

    @Operation(
            summary = "Atualiza o status de um pedido",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Atualizado com sucesso",
                            content = @Content(schema = @Schema(implementation = UpdateStatusPedidoResponse.class))),
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
    @PatchMapping("/{id}/status")
    public CompletableFuture<ResponseEntity<UpdateStatusPedidoResponse>> updatePedidoStatus(
            @PathVariable String id,
            @RequestParam PedidoStatusEvent evento) {
        UpdateStatusPedidoCommand request = new UpdateStatusPedidoCommand(id, evento);
        return updateStatusPedidoHandler.execute(request)
                .thenApplyAsync(response -> responseEntityConverter.convert(response, true));
    }
}
