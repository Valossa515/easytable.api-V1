package br.com.aftersunrise.easytable.borders.dtos.responses;

import br.com.aftersunrise.easytable.borders.entities.ItemCardapio;
import br.com.aftersunrise.easytable.borders.entities.Pedido;

import java.util.List;

public record PedidoResponse(
        String id,
        String mesaIds,
        List<ItemCardapio> itens,
        String dataHora,
        String status
) {
    public static PedidoResponse fromEntity(Pedido pedido) {
        return new PedidoResponse(
                pedido.getId(),
                pedido.getMesaId(),
                pedido.getItens(),
                pedido.getDataHora().toString(),
                pedido.getStatus().name()
        );
    }
}
