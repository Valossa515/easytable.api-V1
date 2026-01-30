package br.com.aftersunrise.easytable.services;

import br.com.aftersunrise.easytable.borders.entities.Pedido;
import br.com.aftersunrise.easytable.repositories.PedidoRepository;
import br.com.aftersunrise.easytable.shared.enums.PedidoStatus;
import br.com.aftersunrise.easytable.shared.enums.PedidoStatusEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaPedidoConsumerService {

    private final ObjectMapper objectMapper;
    private final PedidoRepository pedidoRepository;
    private final RedisService redisService;
    private final PedidoWebSocketPublisher webSocketPublisher;
    private final PedidoStateMachineService pedidoStateMachineService;

    private static final Logger logger = LoggerFactory.getLogger(KafkaPedidoConsumerService.class);

    @KafkaListener(topics = "${app.kafka.topic.pedido-criado}", groupId = "cozinha")
    public void consumirPedido(String mensagem) {
        try {
            Pedido pedido = objectMapper.readValue(mensagem, Pedido.class);
            logger.info("Pedido recebido na cozinha: {}", pedido.getId());

            PedidoStatus novoStatus = pedidoStateMachineService
                    .validarTransicao(pedido.getStatus(), PedidoStatusEvent.INICIAR_PREPARO);

            if (novoStatus == null) {
                logger.error("Transição inválida para o pedido {}: de {} com evento INICIAR_PREPARO",
                        pedido.getId(), pedido.getStatus());
                throw new RuntimeException("Transição de status inválida para o pedido");
            }

            pedido.setStatus(novoStatus);
            pedidoRepository.save(pedido);

            redisService.salvar("pedido:" + pedido.getId(), pedido, 60);

            webSocketPublisher.enviarParaCozinha(pedido); // notifica cozinha

        } catch (Exception e) {
            logger.error("Erro ao processar pedido do Kafka", e);
            throw new RuntimeException("Erro ao processar pedido", e);
        }
    }
}
