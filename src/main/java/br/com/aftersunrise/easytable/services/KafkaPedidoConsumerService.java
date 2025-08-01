package br.com.aftersunrise.easytable.services;

import br.com.aftersunrise.easytable.borders.entities.Pedido;
import br.com.aftersunrise.easytable.repositories.PedidoRepository;
import br.com.aftersunrise.easytable.shared.enums.PedidoStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    private static final Logger logger = LoggerFactory.getLogger(KafkaPedidoConsumerService.class);

    @KafkaListener(topics = "${app.kafka.topic.pedido-criado}", groupId = "cozinha")
    public void consumirPedido(String mensagem) {
        try {
            Pedido pedido = objectMapper.readValue(mensagem, Pedido.class);
            logger.info("Pedido recebido na cozinha: {}", pedido.getId());

            pedido.setStatus(PedidoStatus.EM_PREPARACAO);
            pedidoRepository.save(pedido);

            redisService.salvar("pedido:" + pedido.getId(), pedido, 60);

            webSocketPublisher.enviarParaCozinha(pedido); // notifica cozinha

        } catch (Exception e) {
            logger.error("Erro ao processar pedido do Kafka", e);
            throw new RuntimeException("Erro ao processar pedido", e);
        }
    }
}
