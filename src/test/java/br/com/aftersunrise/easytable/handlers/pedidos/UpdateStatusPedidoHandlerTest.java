package br.com.aftersunrise.easytable.handlers.pedidos;

import br.com.aftersunrise.easytable.borders.adapters.interfaces.IPedidoAdapter;
import br.com.aftersunrise.easytable.borders.dtos.requests.UpdateStatusPedidoCommand;
import br.com.aftersunrise.easytable.borders.dtos.responses.UpdateStatusPedidoResponse;
import br.com.aftersunrise.easytable.repositories.PedidoRepository;
import br.com.aftersunrise.easytable.services.PedidoWebSocketPublisher;
import br.com.aftersunrise.easytable.services.RedisService;
import br.com.aftersunrise.easytable.shared.enums.PedidoStatus;
import br.com.aftersunrise.easytable.shared.handlers.HandlerResponseWithResult;
import br.com.aftersunrise.easytable.shared.properties.MessageResources;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateStatusPedidoHandlerTest {

    @Mock
    private PedidoRepository pedidoRepository;
    @Mock
    private RedisService redisService;
    @Mock
    private IPedidoAdapter pedidoAdapter;
    @Mock
    private PedidoWebSocketPublisher webSocketPublisher;

    private UpdateStatusPedidoHandler handler;

    @BeforeEach
    void setUp() {
        handler = new UpdateStatusPedidoHandler(null, pedidoRepository, redisService, pedidoAdapter, webSocketPublisher);
    }

    @Test
    void deveRetornarNotFoundQuandoPedidoNaoExistir() {
        when(pedidoRepository.findById("pedido-01")).thenReturn(Optional.empty());

        UpdateStatusPedidoCommand command = new UpdateStatusPedidoCommand("pedido-01", PedidoStatus.PRONTO);

        HandlerResponseWithResult<UpdateStatusPedidoResponse> response = handler.execute(command).join();

        assertEquals(404, response.getStatusCode());
        assertEquals("PEDIDO001", response.getMessages().get(0).getCode());
        assertEquals(
                MessageResources.get("error.pedido.not_found"),
                response.getMessages().get(0).getText()
        );
    }
}
