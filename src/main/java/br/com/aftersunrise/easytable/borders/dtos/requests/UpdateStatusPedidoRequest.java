package br.com.aftersunrise.easytable.borders.dtos.requests;

import br.com.aftersunrise.easytable.handlers.validators.annotations.ValidUpdatePedido;
import br.com.aftersunrise.easytable.shared.enums.PedidoStatus;

@ValidUpdatePedido
public record UpdateStatusPedidoRequest(String pedidoId, PedidoStatus status) {
}
