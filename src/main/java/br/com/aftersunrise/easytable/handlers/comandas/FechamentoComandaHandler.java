package br.com.aftersunrise.easytable.handlers.comandas;

import br.com.aftersunrise.easytable.borders.dtos.requests.FechamentoComandaCommand;
import br.com.aftersunrise.easytable.borders.dtos.responses.FechamentoResponse;
import br.com.aftersunrise.easytable.borders.handlers.IFechamentoComandaHandler;
import br.com.aftersunrise.easytable.services.PedidoService;
import br.com.aftersunrise.easytable.shared.exceptions.BusinessException;
import br.com.aftersunrise.easytable.shared.handlers.CommandHandlerBase;
import br.com.aftersunrise.easytable.shared.handlers.HandlerResponseWithResult;
import br.com.aftersunrise.easytable.shared.properties.MessageResources;
import jakarta.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CompletableFuture;

@Service
public class FechamentoComandaHandler extends CommandHandlerBase<FechamentoComandaCommand, FechamentoResponse>
        implements IFechamentoComandaHandler{

    private final PedidoService pedidoService;
    private static final Logger logger = LoggerFactory.getLogger(FechamentoComandaHandler.class);


    public FechamentoComandaHandler(
            PedidoService pedidoService,
            Validator validator, MessageResources messageResources) {
        super(logger, validator);
        this.pedidoService = pedidoService;
    }

    @Override
    @Transactional
    protected CompletableFuture<HandlerResponseWithResult<FechamentoResponse>> doExecute(FechamentoComandaCommand command) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                validarCodigoQR(command.codigoQR());

                FechamentoResponse response = pedidoService.fecharContaPorComanda(command.codigoQR()).join();

                logger.info("Comanda fechada com sucesso: {}", response.comandaId());
                return success(response);

            } catch (BusinessException ex) {
                logger.error("Erro de negócio ao fechar comanda: {}", ex.getMessage());
                return badRequest(ex.getErrorMessage().getCode(), ex.getMessage());
            } catch (RuntimeException ex) {
                logger.error("Erro ao fechar comanda: {}", ex.getMessage());
                return badRequest(
                        MessageResources.get("error.fechamento_comanda_code"),
                        ex.getMessage()
                );
            } catch (Exception ex) {
                logger.error("Erro inesperado ao fechar comanda", ex);
                return internalServerError(
                        MessageResources.get("error.internal_server_error_code"),
                        MessageResources.get("error.internal_server_error")
                );
            }
        });
    }

    private void validarCodigoQR(String codigoQR) {
        if (codigoQR == null || codigoQR.trim().isEmpty()) {
            badRequest(
                    MessageResources.get("error.fechamento_comanda_code"),
                    MessageResources.get("error.fechamento_comanda_invalid_qr"
                    ));
        }
    }
}
