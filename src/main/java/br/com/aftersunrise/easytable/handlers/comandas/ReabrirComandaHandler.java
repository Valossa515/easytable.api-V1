package br.com.aftersunrise.easytable.handlers.comandas;

import br.com.aftersunrise.easytable.borders.dtos.responses.ComandaResponse;
import br.com.aftersunrise.easytable.borders.entities.Comanda;
import br.com.aftersunrise.easytable.borders.handlers.IReabrirComandaHandler;
import br.com.aftersunrise.easytable.services.PedidoService;
import br.com.aftersunrise.easytable.shared.handlers.HandlerBase;
import br.com.aftersunrise.easytable.shared.handlers.HandlerResponseWithResult;
import br.com.aftersunrise.easytable.shared.properties.MessageResources;
import jakarta.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class ReabrirComandaHandler extends HandlerBase<String, ComandaResponse>
        implements IReabrirComandaHandler {

    private static final Logger logger = LoggerFactory.getLogger(ReabrirComandaHandler.class);
    private final PedidoService pedidoService;

    public ReabrirComandaHandler(Validator validator, PedidoService pedidoService) {
        super(logger, validator);
        this.pedidoService = pedidoService;
    }
    @Override
    protected CompletableFuture<HandlerResponseWithResult<ComandaResponse>> doExecute(String request) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                validarCodigoQR(request);

                Comanda comanda = pedidoService.reabrirComanda(request).join();
                logger.info("Comanda reaberta com sucesso: {}", comanda.getId());

                return success(new ComandaResponse(comanda.getId(), comanda.getMesaId(), comanda.isAtiva()));
            } catch (Exception ex) {
                logger.error("Erro ao reabrir comanda", ex);
                return badRequest(
                        MessageResources.get("error.reabrir_comanda_code"),
                        ex.getMessage()
                );
            }
        });
    }

    private void validarCodigoQR(String codigoQR) {
        if (codigoQR == null || codigoQR.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    MessageResources.get("error.reabrir_comanda_invalid_qr"));
        }
    }
}
