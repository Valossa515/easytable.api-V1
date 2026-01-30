package br.com.aftersunrise.easytable.borders.dtos.responses;

import br.com.aftersunrise.easytable.borders.entities.ItemCardapio;
import br.com.aftersunrise.easytable.borders.entities.Pedido;

import java.util.List;

public record PedidoResponse(
        String id,
        String mesaId,
        Integer mesaNumero,
        List<ItemCardapio> itens,
        String dataHora,
        String status
) {
    public static PedidoResponse fromEntity(Pedido pedido) {
        return new PedidoResponse(
                pedido.getId(),
                pedido.getMesaId(),
                null,
                pedido.getItens(),
                pedido.getDataHora().toString(),
                pedido.getStatus().name()
        );
    }

    public PedidoResponse withMesaNumero(Integer numero) {
        return new PedidoResponse(id, mesaId, numero, itens, dataHora, status);
    }
}
