package br.com.aftersunrise.easytable.handlers;

import br.com.aftersunrise.easytable.borders.dtos.requests.ListaPedidosRequest;
import br.com.aftersunrise.easytable.borders.dtos.responses.ListaPedidosResponse;
import br.com.aftersunrise.easytable.borders.dtos.responses.PedidoResponse;
import br.com.aftersunrise.easytable.borders.entities.Pedido;
import br.com.aftersunrise.easytable.handlers.cozinha.CozinhaHandler;
import br.com.aftersunrise.easytable.repositories.PedidoRepository;
import br.com.aftersunrise.easytable.shared.enums.PedidoStatus;
import br.com.aftersunrise.easytable.shared.handlers.HandlerResponseWithResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CozinhaHabdkerTests {

    @Mock
    private PedidoRepository pedidoRepository;

    @InjectMocks
    private CozinhaHandler cozinhaHandler;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void deveListarApenasPedidosNaoProntosOuEntregues() {
        // Arrange
        Pedido pedido1 = new Pedido("1", "Mesa1", List.of(), new Date(), PedidoStatus.EM_PREPARACAO);
        Pedido pedido2 = new Pedido("2", "Mesa2", List.of(), new Date(), PedidoStatus.ENTREGUE);
        Pedido pedido3 = new Pedido("3", "Mesa3", List.of(), new Date(), PedidoStatus.EM_PREPARACAO);
        Pedido pedido4 = new Pedido("4", "Mesa4", List.of(), new Date(), PedidoStatus.PRONTO);

        when(pedidoRepository.findAll()).thenReturn(List.of(pedido1, pedido2, pedido3, pedido4));

        // Act
        CompletableFuture<HandlerResponseWithResult<ListaPedidosResponse>> future =
                cozinhaHandler.execute(new ListaPedidosRequest());
        HandlerResponseWithResult<ListaPedidosResponse> response = future.join();

        // Assert
        assertTrue(response.isSuccess());
        assertNotNull(response.getResult());
        List<PedidoResponse> pedidosFiltrados = response.getResult().pedidos();
        assertEquals(2, pedidosFiltrados.size());

        // Verifique os status convertendo a String para enum ou comparando como String
        for (PedidoResponse dto : pedidosFiltrados) {
            // Opção 1: Comparar como String
            assertEquals("EM_PREPARACAO", dto.status());
        }

        verify(pedidoRepository, times(1)).findAll();
    }

    @Test
    public void deveRetornarErroAoListarPedidos() {
        // Arrange
        when(pedidoRepository.findAll()).thenThrow(new RuntimeException("Erro ao acessar o banco de dados"));

        // Act
        CompletableFuture<HandlerResponseWithResult<ListaPedidosResponse>> future =
                cozinhaHandler.execute(new ListaPedidosRequest());
        HandlerResponseWithResult<ListaPedidosResponse> response = future.join();

        // Assert
        assertFalse(response.isSuccess());
        assertTrue(response.getMessages().stream().anyMatch(m -> "Erro ao listar pedidos".equals(m.getCode())));
        assertNotNull(response.getMessages());

        verify(pedidoRepository, times(1)).findAll();
    }
}
