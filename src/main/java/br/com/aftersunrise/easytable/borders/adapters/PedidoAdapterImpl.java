package br.com.aftersunrise.easytable.borders.adapters;

import br.com.aftersunrise.easytable.borders.adapters.interfaces.IPedidoAdapter;
import br.com.aftersunrise.easytable.borders.dtos.requests.CreatePedidoCommand;
import br.com.aftersunrise.easytable.borders.dtos.requests.UpdateStatusPedidoCommand;
import br.com.aftersunrise.easytable.borders.entities.ItemCardapio;
import br.com.aftersunrise.easytable.borders.entities.Pedido;
import br.com.aftersunrise.easytable.repositories.ItemCardapioRepository;
import br.com.aftersunrise.easytable.services.PedidoStateMachineService;
import br.com.aftersunrise.easytable.shared.enums.PedidoStatus;
import br.com.aftersunrise.easytable.shared.properties.MessageResources;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
public class PedidoAdapterImpl implements IPedidoAdapter {

    private final ItemCardapioRepository itemCardapioRepository;
    private final PedidoStateMachineService pedidoStateMachineService;

public PedidoAdapterImpl(ItemCardapioRepository itemCardapioRepository,
                         PedidoStateMachineService pedidoStateMachineService) {
        this.itemCardapioRepository = itemCardapioRepository;
        this.pedidoStateMachineService = pedidoStateMachineService;
    }

    @Override
    public Pedido toPedido(CreatePedidoCommand request) {
        return Pedido.builder()
                .mesaId(request.mesaId())
                .comandaId(request.comandaId())
                .dataHora(new Date())
                .status(pedidoStateMachineService.getEstadoInicial())
                .build();
    }

    @Override
    public void updatePedido(Pedido pedido, UpdateStatusPedidoCommand request) {
        if (request.evento() == null) {
            throw new IllegalArgumentException(
                    MessageResources.get("error.pedido.evento_required"));
        }

        PedidoStatus proximoStatus = pedidoStateMachineService
                .validarTransicao(pedido.getStatus(), request.evento());

        if (proximoStatus == null) {
            String msg = String.format("Transição inválida: status atual [%s], evento recebido [%s]", 
                pedido.getStatus(), request.evento());
            throw new IllegalArgumentException(msg);
        }

        pedido.setStatus(proximoStatus);
    }
}
