package br.com.aftersunrise.easytable.handlers.pedidos;

import br.com.aftersunrise.easytable.borders.adapters.interfaces.IPedidoAdapter;
import br.com.aftersunrise.easytable.borders.dtos.requests.UpdateStatusPedidoRequest;
import br.com.aftersunrise.easytable.borders.dtos.responses.UpdateStatusPedidoResponse;
import br.com.aftersunrise.easytable.borders.entities.Pedido;
import br.com.aftersunrise.easytable.borders.handlers.IUpdateStatusPedidoHandler;
import br.com.aftersunrise.easytable.repositories.PedidoRepository;
import br.com.aftersunrise.easytable.services.RedisService;
import br.com.aftersunrise.easytable.shared.handlers.HandlerBase;
import br.com.aftersunrise.easytable.shared.handlers.HandlerResponseWithResult;
import br.com.aftersunrise.easytable.shared.properties.MessageResources;
import jakarta.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class UpdateStatusPedidoHandler
        extends HandlerBase<UpdateStatusPedidoRequest, UpdateStatusPedidoResponse>
implements IUpdateStatusPedidoHandler {

    private static final Logger logger = LoggerFactory.getLogger(UpdateStatusPedidoHandler.class);
    private final PedidoRepository pedidoRepository;
    private final RedisService redisService;
    private final IPedidoAdapter pedidoAdapter;

    public UpdateStatusPedidoHandler(
            Validator validator,
            PedidoRepository pedidoRepository,
            RedisService redisService,
            IPedidoAdapter pedidoAdapter) {
        super(logger,validator);
        this.pedidoRepository = pedidoRepository;
        this.redisService = redisService;
        this.pedidoAdapter = pedidoAdapter;
    }


    @Override
    public CompletableFuture<HandlerResponseWithResult<UpdateStatusPedidoResponse>>
    doExecute(UpdateStatusPedidoRequest request) {

        try{
            var pedidoOptional = pedidoRepository.findById(request.pedidoId());

            if (pedidoOptional.isEmpty()) {
                logger.warn("Pedido with ID {} not found", request.pedidoId());
                return CompletableFuture.completedFuture(
                        notFound(MessageResources.get("error.pedido.not_found")));
            }

            Pedido pedido = pedidoOptional.get();

            pedidoAdapter.updatePedido(pedido, request);

            pedidoRepository.save(pedido);

            redisService.salvar("pedido:" + pedido.getId(), pedido, 60);

            logger.info("Status do pedido atualizado para {}", pedido.getStatus());

            return CompletableFuture.completedFuture(
                    success(new UpdateStatusPedidoResponse(
                            MessageResources.get("success.pedido.status_updated"), pedido.getStatus().name())));
        }
        catch (Exception e) {
            logger.error("Erro ao atualizar status do pedido: {}", e.getMessage(), e);
            return CompletableFuture.completedFuture(
                    badRequest(MessageResources.get("error.pedido.status_update_failed"), e.getMessage()));
        }
    }
}