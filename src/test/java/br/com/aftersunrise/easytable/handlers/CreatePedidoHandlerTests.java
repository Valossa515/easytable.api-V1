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
import br.com.aftersunrise.easytable.shared.exceptions.BusinessException;
import br.com.aftersunrise.easytable.shared.handlers.HandlerResponseWithResult;
import br.com.aftersunrise.easytable.shared.properties.MessageResources;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

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

        lenient().when(qrCodeProperties.getBaseUrl()).thenReturn("http://qrcode/");
        lenient().when(qrCodeProperties.getStatusPath()).thenReturn("status/{id}");
        lenient().when(qrCodeProperties.getContaPath()).thenReturn("conta/{id}");
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

    @Test
    void doExecute_DeveRetornarErroQuandoComandaInvalida() throws ExecutionException, InterruptedException {
        // Arrange
        String errorCode = "COM404";
        String errorMessage = "Comanda inválida";
        when(comandaService.validarComanda("comanda456"))
                .thenThrow(new BusinessException(errorCode, errorMessage, HttpStatus.BAD_REQUEST));

        // Act
        CompletableFuture<HandlerResponseWithResult<CreatePedidoResponse>> future = handler.doExecute(request);
        HandlerResponseWithResult<CreatePedidoResponse> response = future.get();

        // Assert
        assertFalse(response.isSuccess());
        assertNull(response.getResult());
        assertEquals(errorMessage, response.getMessages().getFirst().getText());
        assertEquals(errorCode, response.getMessages().getFirst().getCode());

        verify(comandaService).validarComanda("comanda456");
        verifyNoInteractions(pedidoAdapter, itemCardapioRepository, pedidoRepository, redisService, kafkaService);
    }

    @Test
    void doExecute_DeveRetornarErroGenericoQuandoOcorrerExcecaoInesperada() throws ExecutionException, InterruptedException {
        // Arrange
        when(comandaService.validarComanda("comanda456")).thenReturn(comanda);
        when(pedidoAdapter.toPedido(request)).thenReturn(pedido);
        when(itemCardapioRepository.findAllById(request.itensIds()))
                .thenThrow(new RuntimeException("Erro inesperado"));

        // Act
        CompletableFuture<HandlerResponseWithResult<CreatePedidoResponse>> future = handler.doExecute(request);
        HandlerResponseWithResult<CreatePedidoResponse> response = future.get();

        // Assert
        assertFalse(response.isSuccess());
        assertNull(response.getResult());
        assertEquals(MessageResources.get("error.create_item_error"),
                response.getMessages().getFirst().getText());
        assertEquals(MessageResources.get("error.create_item_error_code"),
                response.getMessages().getFirst().getCode());

        verify(comandaService).validarComanda("comanda456");
        verify(pedidoAdapter).toPedido(request);
        verify(itemCardapioRepository).findAllById(request.itensIds());
        verifyNoInteractions(pedidoRepository, redisService, kafkaService);
    }

    @Test
    void doExecute_DeveRetornarErroQuandoItensForemInvalidos() throws ExecutionException, InterruptedException {
        // Arrange
        List<ItemCardapio> itensIncompletos = List.of(itens.get(0)); // Apenas 1 item
        when(comandaService.validarComanda("comanda456")).thenReturn(comanda);
        when(pedidoAdapter.toPedido(request)).thenReturn(pedido);
        when(itemCardapioRepository.findAllById(request.itensIds())).thenReturn(itensIncompletos);

        // Act
        CompletableFuture<HandlerResponseWithResult<CreatePedidoResponse>> future = handler.doExecute(request);
        HandlerResponseWithResult<CreatePedidoResponse> response = future.get();

        // Assert
        assertFalse(response.isSuccess());
        assertNull(response.getResult());
        assertEquals(MessageResources.get("error.invalid_items_code"),
                response.getMessages().getFirst().getCode());
        assertEquals(MessageResources.get("error.invalid_items_code"), // ou outra mensagem específica
                response.getMessages().getFirst().getText());

        verify(comandaService).validarComanda("comanda456");
        verify(pedidoAdapter).toPedido(request);
        verify(itemCardapioRepository).findAllById(request.itensIds());
        verifyNoInteractions(pedidoRepository, redisService, kafkaService);
    }

    @Test
    void doExecute_DeveRetornarErroGenericoQuandoExcecaoNaoEsperadaOcorrer() throws ExecutionException, InterruptedException {
        // Arrange
        when(comandaService.validarComanda("comanda456")).thenReturn(comanda);
        when(pedidoAdapter.toPedido(request)).thenReturn(pedido);
        when(itemCardapioRepository.findAllById(any())).thenReturn(itens);

        // Simula uma exceção genérica (não BusinessException)
        doThrow(new RuntimeException("Simulando erro não esperado"))
                .when(pedidoRepository).save(any(Pedido.class));

        // Act
        CompletableFuture<HandlerResponseWithResult<CreatePedidoResponse>> future = handler.doExecute(request);
        HandlerResponseWithResult<CreatePedidoResponse> response = future.get();

        // Assert
        assertFalse(response.isSuccess());
        assertNull(response.getResult());

        // Verifica se retornou a mensagem genérica do MessageResources
        assertEquals(MessageResources.get("error.create_item_error"),
                response.getMessages().getFirst().getText());

        // Verifica se retornou o código genérico do MessageResources
        assertEquals(MessageResources.get("error.create_item_error_code"),
                response.getMessages().getFirst().getCode());

        // Verifica o status code
        assertEquals(400, response.getStatusCode());

        // Verificações de interação
        verify(pedidoRepository).save(any(Pedido.class));
        verify(kafkaService, never()).enviarPedidoCriado(any());
        verify(redisService, never()).salvar(any(), any(), anyLong());
    }
}