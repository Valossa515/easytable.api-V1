package br.com.aftersunrise.easytable.borders.adapters.interfaces;

import br.com.aftersunrise.easytable.borders.dtos.requests.CreatePedidoRequest;
import br.com.aftersunrise.easytable.borders.dtos.requests.UpdateStatusPedidoRequest;
import br.com.aftersunrise.easytable.borders.entities.Pedido;

public interface IPedidoAdapter {
    Pedido toPedido(CreatePedidoRequest request);
    void updatePedido(Pedido pedido, UpdateStatusPedidoRequest request);
}