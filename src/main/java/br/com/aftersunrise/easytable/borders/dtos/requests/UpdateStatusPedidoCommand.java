package br.com.aftersunrise.easytable.borders.dtos.requests;

import br.com.aftersunrise.easytable.borders.dtos.responses.UpdateStatusPedidoResponse;
import br.com.aftersunrise.easytable.handlers.validators.annotations.ValidUpdatePedido;
import br.com.aftersunrise.easytable.shared.enums.PedidoStatusEvent;
import br.com.aftersunrise.easytable.shared.handlers.ICommand;

@ValidUpdatePedido
public record UpdateStatusPedidoCommand(String pedidoId, PedidoStatusEvent evento)
        implements ICommand<UpdateStatusPedidoResponse> {
}
