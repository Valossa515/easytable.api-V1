package br.com.aftersunrise.easytable.services;

import br.com.aftersunrise.easytable.borders.entities.Pedido;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaPedidoProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.kafka.topic.pedido-criado}")
    private String topic;

    public void enviarPedidoCriado(Pedido pedido) {
        try {
            String pedidoJson = objectMapper.writeValueAsString(pedido);
            kafkaTemplate.send(topic, pedido.getId(), pedidoJson);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Erro ao serializar pedido para Kafka", e);
        }
    }
}
