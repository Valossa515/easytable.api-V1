package br.com.aftersunrise.easytable.handlers.cozinha;

import br.com.aftersunrise.easytable.borders.dtos.requests.ListaPedidosQuery;
import br.com.aftersunrise.easytable.borders.dtos.responses.ListaPedidosResponse;
import br.com.aftersunrise.easytable.borders.dtos.responses.PedidoResponse;
import br.com.aftersunrise.easytable.borders.handlers.IListPedidosQueryHandler;
import br.com.aftersunrise.easytable.repositories.PedidoRepository;
import br.com.aftersunrise.easytable.shared.enums.PedidoStatus;
import br.com.aftersunrise.easytable.shared.handlers.HandlerResponseWithResult;
import br.com.aftersunrise.easytable.shared.handlers.QueryHandlerBase;
import br.com.aftersunrise.easytable.shared.properties.MessageResources;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class CozinhaQueryHandler extends QueryHandlerBase<ListaPedidosQuery, ListaPedidosResponse>
        implements IListPedidosQueryHandler {

    private final PedidoRepository pedidoRepository;

    public CozinhaQueryHandler(
            Validator validator,
            MessageResources messageResources,
            PedidoRepository pedidoRepository
    ) {
        super(validator, messageResources);
        this.pedidoRepository = pedidoRepository;
    }

    @Override
    public CompletableFuture<HandlerResponseWithResult<ListaPedidosResponse>> doExecute(ListaPedidosQuery query) {
        try {
            var pedidos = pedidoRepository.findAll().stream()
                    .filter(p -> p.getStatus() != PedidoStatus.PRONTO && p.getStatus() != PedidoStatus.ENTREGUE
                            && p.getStatus() != PedidoStatus.PAGO)
                    .toList();

            var dtos = pedidos.stream()
                    .map(PedidoResponse::fromEntity)
                    .toList();

            return CompletableFuture.completedFuture(success(new ListaPedidosResponse(dtos)));
        } catch (Exception e) {
            log.error("Erro ao listar pedidos", e);
            return CompletableFuture.completedFuture(
                    internalServerError("Erro ao listar pedidos", e.getMessage()));
        }
    }
}