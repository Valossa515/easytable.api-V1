package br.com.aftersunrise.easytable.handlers.pedidos;

import br.com.aftersunrise.easytable.borders.adapters.interfaces.IPedidoAdapter;
import br.com.aftersunrise.easytable.borders.dtos.requests.UpdateStatusPedidoCommand;
import br.com.aftersunrise.easytable.borders.dtos.responses.UpdateStatusPedidoResponse;
import br.com.aftersunrise.easytable.borders.entities.Pedido;
import br.com.aftersunrise.easytable.borders.handlers.IUpdateStatusPedidoHandler;
import br.com.aftersunrise.easytable.repositories.PedidoRepository;
import br.com.aftersunrise.easytable.services.PedidoWebSocketPublisher;
import br.com.aftersunrise.easytable.services.RedisService;
import br.com.aftersunrise.easytable.shared.enums.PedidoStatus;
import br.com.aftersunrise.easytable.shared.handlers.CommandHandlerBase;
import br.com.aftersunrise.easytable.shared.handlers.HandlerResponseWithResult;
import br.com.aftersunrise.easytable.shared.properties.MessageResources;
import jakarta.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class UpdateStatusPedidoHandler  extends CommandHandlerBase<UpdateStatusPedidoCommand, UpdateStatusPedidoResponse>
        implements IUpdateStatusPedidoHandler {

    private final PedidoRepository pedidoRepository;
    private final RedisService redisService;
    private final IPedidoAdapter pedidoAdapter;
    private final PedidoWebSocketPublisher webSocketPublisher;
    private static final Logger logger = LoggerFactory.getLogger(UpdateStatusPedidoHandler.class);


    public UpdateStatusPedidoHandler(
             Validator validator,
             PedidoRepository pedidoRepository,
             RedisService redisService,
             IPedidoAdapter pedidoAdapter,
             PedidoWebSocketPublisher webSocketPublisher)
    {
        super(logger, validator);
        this.pedidoRepository = pedidoRepository;
        this.redisService = redisService;
        this.pedidoAdapter = pedidoAdapter;
        this.webSocketPublisher = webSocketPublisher;

    }

    @Override
    protected CompletableFuture<HandlerResponseWithResult<UpdateStatusPedidoResponse>> doExecute(UpdateStatusPedidoCommand command) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                var pedidoOptional = pedidoRepository.findById(command.pedidoId());

                if (pedidoOptional.isEmpty()) {
                    logger.warn("Pedido com ID {} não encontrado", command.pedidoId());
                    return notFound("PEDIDO001", MessageResources.get("error.pedido.not_found"));
                }

                Pedido pedido = pedidoOptional.get();

                pedidoAdapter.updatePedido(pedido, command);

                pedidoRepository.save(pedido);

                redisService.salvar("pedido:" + pedido.getId(), pedido, 60);

                if (pedido.getStatus() == PedidoStatus.PRONTO) {
                    redisService.deletar("pedido:" + pedido.getId());
                    webSocketPublisher.removerDaCozinha(pedido.getId());
                    logger.info("Pedido {} marcado como PRONTO, removido do Redis e notificado via WebSocket", pedido.getId());
                }

                logger.info("Status do pedido atualizado para {}", pedido.getStatus());

                return success(new UpdateStatusPedidoResponse(
                        pedido.getId(), pedido.getStatus().name()));
            } catch (Exception e) {
                logger.error("Erro ao atualizar status do pedido: {}", e.getMessage(), e);
                return badRequest("PEDIDO002", MessageResources.get("error.pedido.status_update_failed"));
            }
        });
    }
}