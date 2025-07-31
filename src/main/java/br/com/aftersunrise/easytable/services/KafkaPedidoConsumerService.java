package br.com.aftersunrise.easytable.services;

import br.com.aftersunrise.easytable.borders.entities.Pedido;
import br.com.aftersunrise.easytable.repositories.PedidoRepository;
import br.com.aftersunrise.easytable.shared.enums.PedidoStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaPedidoConsumerService {

    private final ObjectMapper objectMapper;
    private final PedidoRepository pedidoRepository;
    private final RedisService redisService;

    @KafkaListener(topics = "${app.kafka.topic.pedido-criado}", groupId = "cozinha")
    public void consumirPedido(String mensagem) {
        try{
            Pedido pedido = objectMapper.readValue(mensagem, Pedido.class);
            log.info("Pedido recebido na cozinha: {}", pedido.getId());

            pedido.setStatus(PedidoStatus.EM_PREPARACAO);
            pedidoRepository.save(pedido);

            redisService.salvar("pedido:" + pedido.getId(), pedido, 60);
        }
        catch (Exception e) {
            log.error("Erro ao processar pedido do Kafka", e);
            throw new RuntimeException("Erro ao processar pedido", e);
        }
    }

}
