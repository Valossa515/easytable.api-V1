package br.com.aftersunrise.easytable.handlers;

import br.com.aftersunrise.easytable.borders.adapters.interfaces.IPedidoAdapter;
import br.com.aftersunrise.easytable.borders.dtos.requests.CreatePedidoCommand;
import br.com.aftersunrise.easytable.borders.entities.Comanda;
import br.com.aftersunrise.easytable.borders.entities.ItemCardapio;
import br.com.aftersunrise.easytable.borders.entities.Pedido;
import br.com.aftersunrise.easytable.configs.QrCodeProperties;
import br.com.aftersunrise.easytable.handlers.pedidos.CreatePedidoCommandHandler;
import br.com.aftersunrise.easytable.repositories.ComandaRepository;
import br.com.aftersunrise.easytable.repositories.ItemCardapioRepository;
import br.com.aftersunrise.easytable.repositories.PedidoRepository;
import br.com.aftersunrise.easytable.services.ComandaService;
import br.com.aftersunrise.easytable.services.KafkaPedidoProducerService;
import br.com.aftersunrise.easytable.services.RedisService;
import br.com.aftersunrise.easytable.shared.enums.PedidoStatus;
import br.com.aftersunrise.easytable.shared.exceptions.BusinessException;
import br.com.aftersunrise.easytable.shared.models.ErrorMessage;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CreatePedidoHandlerTests {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private IPedidoAdapter pedidoAdapter;

    @Mock
    private KafkaPedidoProducerService kafkaService;

    @Mock
    private RedisService redisService;

    @Mock
    private QrCodeProperties qrCodeProperties;

    @Mock
    private ItemCardapioRepository itemCardapioRepository;

    @Mock
    private ComandaRepository comandaRepository;

    @Mock
    private ComandaService comandaService;

    @Mock
    private Validator validator;

    private CreatePedidoCommandHandler handler;

    @BeforeEach
    void setUp() {
        handler = new CreatePedidoCommandHandler(
                validator,
                pedidoRepository,
                pedidoAdapter,
                kafkaService,
                redisService,
                qrCodeProperties,
                comandaService,
                itemCardapioRepository,
                comandaRepository
        );
    }

    @Test
    void deveCriarPedidoComSucesso() throws Exception {
        // Arrange
        var mesaId = "mesa-1";
        var comandaId = "comanda-1";
        var itemId = "item-1";
        var command = new CreatePedidoCommand(mesaId, List.of(itemId), comandaId);

        var comanda = new Comanda();
        comanda.setId(comandaId);
        comanda.setCodigoQR("qrcode-123");

        var pedido = new Pedido();
        pedido.setId("pedido-1");
        pedido.setMesaId(mesaId);
        pedido.setComandaId(comandaId);
        pedido.setStatus(PedidoStatus.PENDENTE);
        pedido.setDataHora(new Date());

        var item = new ItemCardapio();
        item.setId(itemId);

        pedido.setItens(List.of(item));

        when(comandaService.validarComanda(comandaId, mesaId)).thenReturn(comanda);
        when(itemCardapioRepository.findAllById(List.of(itemId))).thenReturn(List.of(item));
        when(pedidoAdapter.toPedido(command)).thenReturn(pedido);
        when(pedidoRepository.save(any())).thenReturn(pedido);
        when(comandaRepository.findById(comandaId)).thenReturn(Optional.of(comanda));
        when(qrCodeProperties.getBaseUrl()).thenReturn("http://localhost/");
        when(qrCodeProperties.getFechamentoPath()).thenReturn("fechar/{codigoQR}");

        // Act
        var resultFuture = handler.execute(command);
        var result = resultFuture.get();

        // Assert
        assertTrue(result.isSuccess());
        assertNotNull(result.getResult());
        assertEquals("pedido-1", result.getResult().id());
        assertEquals("http://localhost/fechar/qrcode-123", result.getResult().qrCodeUrl());

        verify(redisService).salvar(startsWith("pedido:"), any(Pedido.class), anyLong());
        verify(kafkaService).enviarPedidoCriado(any(Pedido.class));
    }

    @Test
    void deveRetornarErroQuandoItensInvalidos() throws Exception {
        // Arrange
        var command = new CreatePedidoCommand("mesa-1", List.of("item-1", "item-2"), "comanda-1");

        var comanda = new Comanda();
        comanda.setId("comanda-1");
        comanda.setCodigoQR("qrcode");

        var pedido = new Pedido();
        pedido.setItens(List.of(new ItemCardapio())); // só 1 item retornado

        when(comandaService.validarComanda(any(), any())).thenReturn(comanda);
        when(itemCardapioRepository.findAllById(any())).thenReturn(List.of(new ItemCardapio()));
        when(pedidoAdapter.toPedido(command)).thenReturn(pedido);

        // Act
        var result = handler.execute(command).get();

        // Assert
        assertFalse(result.isSuccess());
        assertNotNull(result.getMessages());
        assertFalse(result.getMessages().isEmpty());
        var firstMessage = result.getMessages().get(0);
        assertEquals("400!", firstMessage.getCode());
    }

    @Test
    void deveRetornarErroQuandoComandaNaoExiste() throws Exception {
        // Arrange
        var command = new CreatePedidoCommand("mesa-1", List.of("item-1"), "comanda-nao-existe");

        when(comandaService.validarComanda(any(), any()))
                .thenThrow(new BusinessException(new ErrorMessage("COM404", "Comanda não encontrada"), HttpStatus.NOT_FOUND));

        // Act
        var result = handler.execute(command).get();

        // Assert
        assertFalse(result.isSuccess());

        // Verifique se a lista de mensagens não está vazia e se o código da primeira mensagem é o esperado.
        assertNotNull(result.getMessages());
        assertFalse(result.getMessages().isEmpty());

        // Obtenha a primeira mensagem da lista
        var firstMessage = result.getMessages().get(0);

        // Agora compare o código da mensagem com o valor esperado
        assertEquals("COM404", firstMessage.getCode());
    }

    @Test
    void deveTratarErroGenerico() throws Exception {
        // Arrange
        var command = new CreatePedidoCommand("mesa-1", List.of("item-1"), "comanda-1");

        when(comandaService.validarComanda(any(), any())).thenThrow(new RuntimeException("Falha interna"));

        // Act
        var result = handler.execute(command).get();

        // Assert
        assertFalse(result.isSuccess());

        // Verifique se a lista de mensagens não está vazia.
        assertNotNull(result.getMessages());
        assertFalse(result.getMessages().isEmpty());

        // Obtenha a primeira mensagem da lista.
        var firstMessage = result.getMessages().get(0);

        // Verifique o código da primeira mensagem.
        assertEquals("Erro inesperado ao persistir ações do usuário....", firstMessage.getCode());
    }
}