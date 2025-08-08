package br.com.aftersunrise.easytable.borders.dtos.requests;

import br.com.aftersunrise.easytable.borders.dtos.responses.PedidoResponse;
import br.com.aftersunrise.easytable.borders.dtos.responses.UpdateStatusPedidoResponse;
import br.com.aftersunrise.easytable.handlers.validators.annotations.ValidUpdatePedido;
import br.com.aftersunrise.easytable.shared.enums.PedidoStatus;
import br.com.aftersunrise.easytable.shared.handlers.ICommand;

@ValidUpdatePedido
public record UpdateStatusPedidoCommand(String pedidoId, PedidoStatus status) implements ICommand<UpdateStatusPedidoResponse> {
}
