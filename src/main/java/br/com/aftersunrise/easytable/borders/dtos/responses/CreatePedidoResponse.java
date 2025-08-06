package br.com.aftersunrise.easytable.borders.dtos.responses;

import br.com.aftersunrise.easytable.borders.entities.ItemCardapio;
import br.com.aftersunrise.easytable.shared.enums.PedidoStatus;

import java.util.Date;
import java.util.List;

public record CreatePedidoResponse(
        String id,
        String mesaId,
        String comandaId,
        List<ItemCardapio> itens,
        Date dataHora,
        PedidoStatus status,
        String qrCodeUrl
) { }
