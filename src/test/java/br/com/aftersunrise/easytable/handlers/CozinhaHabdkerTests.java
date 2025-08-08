package br.com.aftersunrise.easytable.handlers;

import br.com.aftersunrise.easytable.borders.dtos.requests.ListaPedidosQuery;
import br.com.aftersunrise.easytable.borders.dtos.responses.ListaPedidosResponse;
import br.com.aftersunrise.easytable.borders.entities.ItemCardapio;
import br.com.aftersunrise.easytable.borders.entities.Pedido;
import br.com.aftersunrise.easytable.handlers.cozinha.CozinhaQueryHandler;
import br.com.aftersunrise.easytable.repositories.PedidoRepository;
import br.com.aftersunrise.easytable.shared.enums.PedidoStatus;
import br.com.aftersunrise.easytable.shared.handlers.HandlerResponseWithResult;
import br.com.aftersunrise.easytable.shared.properties.MessageResources;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CozinhaHabdkerTests {
    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private Validator validator;

    @Mock
    private MessageResources messageResources;

    @InjectMocks
    private CozinhaQueryHandler handler;

    private ListaPedidosQuery query;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        query = new ListaPedidosQuery();
    }

    @Test
    void deveRetornarApenasPedidosAindaNaoFinalizados() throws ExecutionException, InterruptedException {
        // Arrange
        var item1 = ItemCardapio.builder()
                .nome("nome_do_item")
                .preco(10.0)
                .build();

        List<ItemCardapio> itens = List.of(item1);

        var pedidoEmAndamento = Pedido.builder()
                .mesaId("mesa-1")
                .comandaId("comanda-1")
                .itens(itens)
                .status(PedidoStatus.EM_PREPARACAO)
                .dataHora(new Date()) // Adicione esta linha
                .build();

        List<Pedido> pedidosMock = List.of(
                pedidoEmAndamento
        );

        when(pedidoRepository.findAll()).thenReturn(pedidosMock);

        // Act
        HandlerResponseWithResult<ListaPedidosResponse> result = handler.doExecute(query).get();

        // Assert
        assertTrue(result.isSuccess());
        assertEquals(1, result.getResult().pedidos().size());
        assertEquals(pedidoEmAndamento.getId(), result.getResult().pedidos().get(0).id());
    }
}
