package br.com.aftersunrise.easytable.services;

import br.com.aftersunrise.easytable.borders.dtos.responses.PedidoResponse;
import br.com.aftersunrise.easytable.borders.entities.Pedido;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PedidoWebSocketPublisher {

    private final SimpMessagingTemplate messagingTemplate;

    public void enviarParaCozinha(Pedido pedido) {
        messagingTemplate.convertAndSend("/topic/pedidos", PedidoResponse.fromEntity(pedido));
    }
}