package br.com.aftersunrise.easytable.services;

import br.com.aftersunrise.easytable.borders.dtos.responses.PedidoResponse;
import br.com.aftersunrise.easytable.borders.entities.Pedido;
import br.com.aftersunrise.easytable.repositories.MesaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PedidoWebSocketPublisher {

    private final SimpMessagingTemplate messagingTemplate;
    private final MesaRepository mesaRepository;

    public void enviarParaCozinha(Pedido pedido) {
        Integer mesaNumero = mesaRepository.findById(pedido.getMesaId())
                .map(mesa -> mesa.getNumero())
                .orElse(null);

        PedidoResponse response = PedidoResponse.fromEntity(pedido)
                .withMesaNumero(mesaNumero);

        messagingTemplate.convertAndSend("/topic/pedidos", response);
    }

    public void removerDaCozinha(String pedidoId) {
        messagingTemplate.convertAndSend("/topic/pedidos/remover", pedidoId);
    }
}