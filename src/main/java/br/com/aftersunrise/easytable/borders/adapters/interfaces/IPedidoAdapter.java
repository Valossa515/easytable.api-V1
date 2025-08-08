package br.com.aftersunrise.easytable.borders.adapters.interfaces;

import br.com.aftersunrise.easytable.borders.dtos.requests.CreatePedidoCommand;
import br.com.aftersunrise.easytable.borders.dtos.requests.UpdateStatusPedidoCommand;
import br.com.aftersunrise.easytable.borders.entities.Pedido;

public interface IPedidoAdapter {
    Pedido toPedido(CreatePedidoCommand request);
    void updatePedido(Pedido pedido, UpdateStatusPedidoCommand request);
}