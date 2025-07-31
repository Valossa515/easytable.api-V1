package br.com.aftersunrise.easytable.handlers.pedidos;

import br.com.aftersunrise.easytable.borders.adapters.interfaces.IPedidoAdapter;
import br.com.aftersunrise.easytable.borders.dtos.requests.CreatePedidoRequest;
import br.com.aftersunrise.easytable.borders.dtos.responses.CreatePedidoResponse;
import br.com.aftersunrise.easytable.borders.entities.Comanda;
import br.com.aftersunrise.easytable.borders.entities.ItemCardapio;
import br.com.aftersunrise.easytable.borders.entities.Pedido;
import br.com.aftersunrise.easytable.borders.handlers.ICreatePedidoHandler;
import br.com.aftersunrise.easytable.configs.QrCodeProperties;
import br.com.aftersunrise.easytable.repositories.ItemCardapioRepository;
import br.com.aftersunrise.easytable.repositories.PedidoRepository;
import br.com.aftersunrise.easytable.services.ComandaService;
import br.com.aftersunrise.easytable.services.KafkaPedidoProducerService;
import br.com.aftersunrise.easytable.services.RedisService;
import br.com.aftersunrise.easytable.shared.enums.PedidoStatus;
import br.com.aftersunrise.easytable.shared.handlers.HandlerBase;
import br.com.aftersunrise.easytable.shared.handlers.HandlerResponseWithResult;
import br.com.aftersunrise.easytable.shared.properties.MessageResources;
import jakarta.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class CreatePedidoHandler extends HandlerBase<CreatePedidoRequest, CreatePedidoResponse>
        implements ICreatePedidoHandler {

    private static final Logger logger = LoggerFactory.getLogger(CreatePedidoHandler.class);
    private final PedidoRepository pedidoRepository;
    private final IPedidoAdapter pedidoAdapter;
    private final KafkaPedidoProducerService kafkaService;
    private final RedisService redisService;
    private final QrCodeProperties qrCodeProperties;
    private final ComandaService comandaService;
    private final ItemCardapioRepository itemCardapioRepository;

    public CreatePedidoHandler(
            Validator validator,
            PedidoRepository pedidoRepository,
            IPedidoAdapter pedidoAdapter,
            KafkaPedidoProducerService kafkaService,
            RedisService redisService,
            QrCodeProperties qrCodeProperties,
            ComandaService comandaService,
            ItemCardapioRepository itemCardapioRepository) {
        super(logger, validator);
        this.pedidoRepository = pedidoRepository;
        this.pedidoAdapter = pedidoAdapter;
        this.kafkaService = kafkaService;
        this.redisService = redisService;
        this.qrCodeProperties = qrCodeProperties;
        this.comandaService = comandaService;
        this.itemCardapioRepository = itemCardapioRepository;
    }

    @Override
    @Transactional
    protected CompletableFuture<HandlerResponseWithResult<CreatePedidoResponse>> doExecute(CreatePedidoRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Comanda comanda = validarComanda(request.comandaId());
                Pedido pedido = montarPedido(request, comanda);
                validarItens(pedido, request.itensIds());
                Pedido pedidoSalvo = salvarPedido(pedido);
                posSalvar(pedidoSalvo);
                CreatePedidoResponse response = criarResponse(pedidoSalvo);
                logger.info("Pedido feito com sucesso: {}", pedidoSalvo);
                return created(response);
            } catch (Exception ex) {
                logger.error("Erro ao criar o pedido", ex);
                return badRequest(MessageResources.get("error.create_item_error_code"),
                        MessageResources.get("error.create_item_error"));
            }
        });
    }

    public Comanda validarComanda(String comandaId) {
        return comandaService.validarComanda(comandaId);
    }

    public void posSalvar(Pedido pedidoSalvo) {
        redisService.salvar("pedido:" + pedidoSalvo.getId(), pedidoSalvo, 60);
        kafkaService.enviarPedidoCriado(pedidoSalvo);
    }

    public Pedido montarPedido(CreatePedidoRequest request, Comanda comanda) {
        Pedido pedido = pedidoAdapter.toPedido(request);
        pedido.setComandaId(comanda.getId());
        pedido.setDataHora(new Date());
        pedido.setStatus(PedidoStatus.PENDENTE);
        List<ItemCardapio> itensCompletos = itemCardapioRepository.findAllById(request.itensIds());
        pedido.setItens(itensCompletos);
        return pedido;
    }

    public void validarItens(Pedido pedido, List<String> itensIds) {
        if (pedido.getItens().size() != itensIds.size()) {
            throw new IllegalArgumentException(MessageResources.get("error.invalid_items_code"));
        }
    }

    public Pedido salvarPedido(Pedido pedido) {
        return pedidoRepository.save(pedido);
    }

    public CreatePedidoResponse criarResponse(Pedido pedidoSalvo) {
        String qrCodeAcompanhamentoUrl = qrCodeProperties.getBaseUrl() +
                qrCodeProperties.getStatusPath().replace("{id}", pedidoSalvo.getId());
        String qrCodeContaUrl = qrCodeProperties.getBaseUrl() +
                qrCodeProperties.getContaPath().replace("{id}", pedidoSalvo.getId());

        return new CreatePedidoResponse(
                pedidoSalvo.getId(),
                pedidoSalvo.getMesaId(),
                pedidoSalvo.getComandaId(),
                pedidoSalvo.getItens(),
                pedidoSalvo.getDataHora(),
                pedidoSalvo.getStatus(),
                qrCodeAcompanhamentoUrl,
                qrCodeContaUrl
        );
    }
}
