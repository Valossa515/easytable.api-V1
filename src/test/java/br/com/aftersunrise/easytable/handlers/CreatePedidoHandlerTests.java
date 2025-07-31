package br.com.aftersunrise.easytable.handlers;

import br.com.aftersunrise.easytable.borders.adapters.interfaces.IPedidoAdapter;
import br.com.aftersunrise.easytable.borders.dtos.requests.CreatePedidoRequest;
import br.com.aftersunrise.easytable.borders.dtos.responses.CreatePedidoResponse;
import br.com.aftersunrise.easytable.borders.entities.Comanda;
import br.com.aftersunrise.easytable.borders.entities.ItemCardapio;
import br.com.aftersunrise.easytable.borders.entities.Pedido;
import br.com.aftersunrise.easytable.builders.CreatePedidoRequestBuilder;
import br.com.aftersunrise.easytable.configs.QrCodeProperties;
import br.com.aftersunrise.easytable.handlers.pedidos.CreatePedidoHandler;
import br.com.aftersunrise.easytable.repositories.ComandaRepository;
import br.com.aftersunrise.easytable.repositories.ItemCardapioRepository;
import br.com.aftersunrise.easytable.repositories.PedidoRepository;
import br.com.aftersunrise.easytable.services.ComandaService;
import br.com.aftersunrise.easytable.services.KafkaPedidoProducerService;
import br.com.aftersunrise.easytable.services.RedisService;
import br.com.aftersunrise.easytable.shared.enums.PedidoStatus;
import br.com.aftersunrise.easytable.shared.handlers.HandlerResponseWithResult;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CreatePedidoHandlerTests {

    @Mock
    private Validator validator;

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private IPedidoAdapter pedidoAdapter;

    @Mock
    private ComandaRepository comandaRepository;

    @Mock
    private ItemCardapioRepository itemCardapioRepository;

    @Mock
    private KafkaPedidoProducerService kafkaService;

    @Mock
    private RedisService redisService;

    @Mock
    private QrCodeProperties qrCodeProperties;

    @Mock
    private ComandaService comandaService;

    @InjectMocks
    private CreatePedidoHandler handler;

    private CreatePedidoRequest request;
    private Comanda comanda;
    private Pedido pedido;
    private List<ItemCardapio> itens;
    private Pedido pedidoSalvo;

    @BeforeEach
    public void setUp() {
        CreatePedidoRequestBuilder builder = CreatePedidoRequestBuilder.builder()
                .mesaId("mesa123")
                .comandaId("comanda456")
                .itensIds(Arrays.asList("item1", "item2"))
                .dataHora(new Date())
                .status(PedidoStatus.PENDENTE)
                .qrCodeAcompanhamento("qrCode123");

        request = builder.toCreatePedidoRequest();

        comanda = Comanda.builder()
                .id("comanda456")
                .codigoQR("abc123")
                .mesaId("mesa001")
                .ativa(true)
                .dataCriacao(new Date())
                .qrCodeImagem(null)
                .build();

        ItemCardapio item1 = ItemCardapio.builder()
                .id("item1")
                .nome("Item 1")
                .preco(10.0)
                .build();

        ItemCardapio item2 = ItemCardapio.builder()
                .id("item2")
                .nome("Item 2")
                .preco(15.0)
                .build();

        itens = Arrays.asList(item1, item2);

        pedido = Pedido.builder()
                .id("pedido123")
                .comandaId(comanda.getId())
                .mesaId(request.mesaId())
                .dataHora(new Date())
                .status(PedidoStatus.PENDENTE)
                .itens(itens)
                .build();

        pedidoSalvo = pedido;

        when(qrCodeProperties.getBaseUrl()).thenReturn("http://qrcode/");
        when(qrCodeProperties.getStatusPath()).thenReturn("status/{id}");
        when(qrCodeProperties.getContaPath()).thenReturn("conta/{id}");
    }

    @Test
    void doExecute_DeveCriarPedidoComSucesso() throws ExecutionException, InterruptedException {
        // Arrange
        when(comandaService.validarComanda("comanda456")).thenReturn(comanda);
        when(pedidoAdapter.toPedido(request)).thenReturn(pedido);
        when(itemCardapioRepository.findAllById(request.itensIds())).thenReturn(itens);
        when(pedidoRepository.save(pedido)).thenReturn(pedidoSalvo);

        // Act
        CompletableFuture<HandlerResponseWithResult<CreatePedidoResponse>> future = handler.doExecute(request);
        HandlerResponseWithResult<CreatePedidoResponse> response = future.get();

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("pedido123", response.getResult().id());
        assertEquals("mesa123", response.getResult().mesaId());
        assertEquals("comanda456", response.getResult().comandaId());
        assertEquals(2, response.getResult().itens().size());
        assertEquals(PedidoStatus.PENDENTE, response.getResult().status());
        assertNotNull(response.getResult().dataHora());
        assertTrue(response.getResult().qrCodeAcompanhamentoUrl().contains("pedido123"));
        assertTrue(response.getResult().qrCodeContaUrl().contains("pedido123"));

        // Verifica se os métodos foram chamados corretamente
        verify(comandaService).validarComanda("comanda456");
        verify(pedidoAdapter).toPedido(request);
        verify(itemCardapioRepository).findAllById(request.itensIds());
        verify(pedidoRepository).save(pedido);
        verify(redisService).salvar(eq("pedido:pedido123"), any(Pedido.class), eq(60L)); // Alterado para 60L
        verify(kafkaService).enviarPedidoCriado(pedidoSalvo);
    }
}