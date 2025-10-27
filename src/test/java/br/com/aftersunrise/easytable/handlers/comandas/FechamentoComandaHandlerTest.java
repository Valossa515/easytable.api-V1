package br.com.aftersunrise.easytable.handlers.comandas;

import br.com.aftersunrise.easytable.borders.dtos.requests.FechamentoComandaCommand;
import br.com.aftersunrise.easytable.borders.dtos.responses.FechamentoResponse;
import br.com.aftersunrise.easytable.services.PedidoService;
import br.com.aftersunrise.easytable.shared.handlers.HandlerResponseWithResult;
import br.com.aftersunrise.easytable.shared.properties.MessageResources;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FechamentoComandaHandlerTest {

    @Mock
    private PedidoService pedidoService;

    private FechamentoComandaHandler handler;

    @BeforeEach
    void setUp() {
        handler = new FechamentoComandaHandler(pedidoService, null, null);
    }

    @Test
    void deveRetornarBadRequestQuandoCodigoQrForInvalido() {
        FechamentoComandaCommand command = new FechamentoComandaCommand("   ");

        HandlerResponseWithResult<FechamentoResponse> response = handler.execute(command).join();

        assertEquals(400, response.getStatusCode());
        assertEquals("400!", response.getMessages().get(0).getCode());
        assertEquals(
                MessageResources.get("error.fechamento_comanda_invalid_qr"),
                response.getMessages().get(0).getText()
        );
        assertEquals(1, response.getMessages().size());
        verifyNoInteractions(pedidoService);
    }

    @Test
    void deveFecharComandaQuandoCodigoQrForValido() {
        FechamentoResponse esperado = new FechamentoResponse("comanda", Collections.emptyList(), BigDecimal.ONE, "sucesso");
        when(pedidoService.fecharContaPorComanda("123")).thenReturn(CompletableFuture.completedFuture(esperado));

        HandlerResponseWithResult<FechamentoResponse> response = handler.execute(new FechamentoComandaCommand("123")).join();

        assertEquals(200, response.getStatusCode());
        assertEquals(esperado, response.getResult());
        assertTrue(response.getMessages().isEmpty());
        verify(pedidoService).fecharContaPorComanda("123");
    }
}
