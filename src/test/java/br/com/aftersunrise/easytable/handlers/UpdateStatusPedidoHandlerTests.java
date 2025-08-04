package br.com.aftersunrise.easytable.handlers;

import br.com.aftersunrise.easytable.borders.adapters.interfaces.IPedidoAdapter;
import br.com.aftersunrise.easytable.borders.dtos.requests.UpdateStatusPedidoRequest;
import br.com.aftersunrise.easytable.borders.entities.Pedido;
import br.com.aftersunrise.easytable.handlers.pedidos.UpdateStatusPedidoHandler;
import br.com.aftersunrise.easytable.repositories.PedidoRepository;
import br.com.aftersunrise.easytable.services.RedisService;
import br.com.aftersunrise.easytable.shared.enums.PedidoStatus;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UpdateStatusPedidoHandlerTests {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private RedisService redisService;

    @Mock
    private IPedidoAdapter pedidoAdapter;

    @Mock
    private Validator validator;

    @InjectMocks
    private UpdateStatusPedidoHandler handler;


    @BeforeEach
    void setUp() {
        String pedidoId = UUID.randomUUID().toString();
        UpdateStatusPedidoRequest request = new UpdateStatusPedidoRequest(pedidoId, PedidoStatus.EM_PREPARACAO);
    }

    @Test
    void testUpdateStatusPedidoSuccess() {
        String pedidoId = UUID.randomUUID().toString();
        UpdateStatusPedidoRequest request = new UpdateStatusPedidoRequest(pedidoId, PedidoStatus.EM_PREPARACAO);
        Pedido pedido = new Pedido();
        pedido.setId(pedidoId);
        pedido.setStatus(PedidoStatus.PENDENTE);

        when(pedidoRepository.findById(pedidoId)).thenReturn(Optional.of(pedido));
        doAnswer(invocation -> {
            Pedido updatedPedido = invocation.getArgument(0);
            updatedPedido.setStatus(request.status());
            return null;
        }).when(pedidoAdapter).updatePedido(pedido, request);
        when(pedidoRepository.save(pedido)).thenReturn(pedido);
        var response = handler.execute(request).join();

        assertTrue(response.isSuccess());
    }

    @Test
    void testUpdateStatusPedidoNotFound() {
        String pedidoId = UUID.randomUUID().toString();
        UpdateStatusPedidoRequest request = new UpdateStatusPedidoRequest(pedidoId, PedidoStatus.EM_PREPARACAO);

        when(pedidoRepository.findById(pedidoId)).thenReturn(Optional.empty());

        var response = handler.execute(request).join();

        assertFalse(response.isSuccess());
        assertEquals("Pedido não encontrado.", response.getMessages().getFirst().getText());
    }

    @Test
    void testUpdateStatusPedidoException() {
        String pedidoId = UUID.randomUUID().toString();
        UpdateStatusPedidoRequest request = new UpdateStatusPedidoRequest(pedidoId, PedidoStatus.EM_PREPARACAO);

        when(pedidoRepository.findById(pedidoId)).thenThrow(new RuntimeException("Database error"));

        var response = handler.execute(request).join();

        assertFalse(response.isSuccess());
        assertEquals("Database error", response.getMessages().getFirst().getText());
    }

    @Test
    void testRemoveFromReisPedidoWhenPedidoStatusEqualPronto() {
        String pedidoId = UUID.randomUUID().toString();
        UpdateStatusPedidoRequest request = new UpdateStatusPedidoRequest(pedidoId, PedidoStatus.PRONTO);
        Pedido pedido = new Pedido();
        pedido.setId(pedidoId);
        pedido.setStatus(PedidoStatus.EM_PREPARACAO);

        when(pedidoRepository.findById(pedidoId)).thenReturn(Optional.of(pedido));
        doAnswer(invocation -> {
            Pedido updatedPedido = invocation.getArgument(0);
            updatedPedido.setStatus(request.status());
            return null;
        }).when(pedidoAdapter).updatePedido(pedido, request);
        when(pedidoRepository.save(pedido)).thenReturn(pedido);

        handler.execute(request).join();

        assertEquals(PedidoStatus.PRONTO, pedido.getStatus());
    }

}
