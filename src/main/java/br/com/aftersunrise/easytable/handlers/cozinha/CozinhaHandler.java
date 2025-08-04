package br.com.aftersunrise.easytable.handlers.cozinha;

import br.com.aftersunrise.easytable.borders.dtos.requests.ListaPedidosRequest;
import br.com.aftersunrise.easytable.borders.dtos.responses.ListaPedidosResponse;
import br.com.aftersunrise.easytable.borders.dtos.responses.PedidoResponse;
import br.com.aftersunrise.easytable.borders.handlers.IListPedidosHandler;
import br.com.aftersunrise.easytable.repositories.PedidoRepository;
import br.com.aftersunrise.easytable.shared.enums.PedidoStatus;
import br.com.aftersunrise.easytable.shared.handlers.HandlerBase;
import br.com.aftersunrise.easytable.shared.handlers.HandlerResponseWithResult;
import jakarta.validation.Validator;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class CozinhaHandler
        extends HandlerBase<ListaPedidosRequest, ListaPedidosResponse>
        implements IListPedidosHandler {

    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(CozinhaHandler.class);
    private final PedidoRepository pedidoRepository;

    public CozinhaHandler(
            Validator validator,
            PedidoRepository pedidoRepository) {
        super(logger, validator);
        this.pedidoRepository = pedidoRepository;
    }

    @Override
    protected CompletableFuture<HandlerResponseWithResult<ListaPedidosResponse>> doExecute(ListaPedidosRequest request) {
        try {
            var pedidos = pedidoRepository.findAll().stream()
                    .filter(p -> p.getStatus() != PedidoStatus.PRONTO && p.getStatus() != PedidoStatus.ENTREGUE)
                    .toList();

            var dtos = pedidos.stream()
                    .map(PedidoResponse::fromEntity)
                    .toList();

            return CompletableFuture.completedFuture(success(new ListaPedidosResponse(dtos)));
        } catch (Exception e) {
            logger.error("Erro ao listar pedidos", e);
            return CompletableFuture.completedFuture(
                    badRequest("Erro ao listar pedidos", e.getMessage()));
        }
    }
}