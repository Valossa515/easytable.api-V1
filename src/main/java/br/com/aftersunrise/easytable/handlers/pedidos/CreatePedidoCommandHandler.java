package br.com.aftersunrise.easytable.handlers.pedidos;

import br.com.aftersunrise.easytable.borders.adapters.interfaces.IPedidoAdapter;
import br.com.aftersunrise.easytable.borders.dtos.requests.CreatePedidoCommand;
import br.com.aftersunrise.easytable.borders.dtos.responses.CreatePedidoResponse;
import br.com.aftersunrise.easytable.borders.entities.Comanda;
import br.com.aftersunrise.easytable.borders.entities.ItemCardapio;
import br.com.aftersunrise.easytable.borders.entities.Pedido;
import br.com.aftersunrise.easytable.borders.handlers.ICreatePedidoHandler;
import br.com.aftersunrise.easytable.configs.QrCodeProperties;
import br.com.aftersunrise.easytable.repositories.ComandaRepository;
import br.com.aftersunrise.easytable.repositories.ItemCardapioRepository;
import br.com.aftersunrise.easytable.repositories.PedidoRepository;
import br.com.aftersunrise.easytable.services.ComandaService;
import br.com.aftersunrise.easytable.services.KafkaPedidoProducerService;
import br.com.aftersunrise.easytable.services.RedisService;
import br.com.aftersunrise.easytable.shared.enums.PedidoStatus;
import br.com.aftersunrise.easytable.shared.exceptions.BusinessException;
import br.com.aftersunrise.easytable.shared.handlers.CommandHandlerBase;
import br.com.aftersunrise.easytable.shared.handlers.HandlerResponseWithResult;
import br.com.aftersunrise.easytable.shared.models.ErrorMessage;
import br.com.aftersunrise.easytable.shared.properties.MessageResources;
import jakarta.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class CreatePedidoCommandHandler extends CommandHandlerBase<CreatePedidoCommand, CreatePedidoResponse>
        implements ICreatePedidoHandler {

    private static final Logger logger = LoggerFactory.getLogger(CreatePedidoCommandHandler.class);
    private final PedidoRepository pedidoRepository;
    private final IPedidoAdapter pedidoAdapter;
    private final KafkaPedidoProducerService kafkaService;
    private final RedisService redisService;
    private final QrCodeProperties qrCodeProperties;
    private final ComandaService comandaService;
    private final ItemCardapioRepository itemCardapioRepository;
    private final ComandaRepository comandaRepository;


    public CreatePedidoCommandHandler(
            Validator validator,
            PedidoRepository pedidoRepository,
            IPedidoAdapter pedidoAdapter,
            KafkaPedidoProducerService kafkaService,
            RedisService redisService,
            QrCodeProperties qrCodeProperties,
            ComandaService comandaService,
            ItemCardapioRepository itemCardapioRepository,
            ComandaRepository comandaRepository) {

        super(logger, validator);
        this.pedidoRepository = pedidoRepository;
        this.pedidoAdapter = pedidoAdapter;
        this.kafkaService = kafkaService;
        this.redisService = redisService;
        this.qrCodeProperties = qrCodeProperties;
        this.comandaService = comandaService;
        this.itemCardapioRepository = itemCardapioRepository;
        this.comandaRepository = comandaRepository;
    }


    @Override
    @Transactional
    protected CompletableFuture<HandlerResponseWithResult<CreatePedidoResponse>> doExecute(CreatePedidoCommand command) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Comanda comanda = validarComanda(command.comandaId(), command.mesaId());
                Pedido pedido = montarPedido(command, comanda);
                validarItens(pedido, command.itensIds());
                Pedido pedidoSalvo = salvarPedido(pedido);
                posSalvar(pedidoSalvo);
                CreatePedidoResponse resultDto = criarResultado(pedidoSalvo);
                return success(resultDto);
            } catch (BusinessException ex) {
                return badRequest(ex.getErrorMessage().getCode(), ex.getMessage());
            } catch (IllegalArgumentException ex) {
                return badRequest(
                        MessageResources.get("error.invalid_items_code"),
                        ex.getMessage()
                );
            } catch (Exception ex) {
                return badRequest(
                        MessageResources.get("error.create_item_error_code"),
                        MessageResources.get("error.create_item_error")
                );
            }
        });
    }

    private Comanda validarComanda(String comandaId, String mesaId) {
        return comandaService.validarComanda(comandaId, mesaId);
    }

    private void posSalvar(Pedido pedidoSalvo) {
        redisService.salvar("pedido:" + pedidoSalvo.getId(), pedidoSalvo, 60);
        kafkaService.enviarPedidoCriado(pedidoSalvo);
    }

    private Pedido montarPedido(CreatePedidoCommand command, Comanda comanda) {
        Pedido pedido = pedidoAdapter.toPedido(command);
        pedido.setComandaId(comanda.getId());
        pedido.setDataHora(new Date());
        pedido.setStatus(PedidoStatus.PENDENTE);
        List<ItemCardapio> itensCompletos = itemCardapioRepository.findAllById(command.itensIds());
        pedido.setItens(itensCompletos);
        return pedido;
    }

    private void validarItens(Pedido pedido, List<String> itensIds) {
        if (pedido.getItens().size() != itensIds.size()) {
            throw new IllegalArgumentException(MessageResources.get("error.invalid_items_code"));
        }
    }

    private Pedido salvarPedido(Pedido pedido) {
        return pedidoRepository.save(pedido);
    }

    private CreatePedidoResponse criarResultado(Pedido pedidoSalvo) {
        Comanda comanda = comandaRepository.findById(pedidoSalvo.getComandaId())
                .orElseThrow(() -> new BusinessException(
                        new ErrorMessage("COM404", "Comanda não encontrada"),
                        HttpStatus.NOT_FOUND
                ));

        String qrCodeFechamentoUrl = qrCodeProperties.getBaseUrl() +
                qrCodeProperties.getFechamentoPath().replace("{codigoQR}", comanda.getCodigoQR());

        return new CreatePedidoResponse(
                pedidoSalvo.getId(),
                pedidoSalvo.getMesaId(),
                pedidoSalvo.getComandaId(),
                pedidoSalvo.getItens(),
                pedidoSalvo.getDataHora(),
                pedidoSalvo.getStatus(),
                qrCodeFechamentoUrl
        );
    }
}
