package br.com.aftersunrise.easytable.borders.adapters;

import br.com.aftersunrise.easytable.borders.adapters.interfaces.IPedidoAdapter;
import br.com.aftersunrise.easytable.borders.dtos.requests.CreatePedidoCommand;
import br.com.aftersunrise.easytable.borders.dtos.requests.UpdateStatusPedidoCommand;
import br.com.aftersunrise.easytable.borders.entities.ItemCardapio;
import br.com.aftersunrise.easytable.borders.entities.Pedido;
import br.com.aftersunrise.easytable.repositories.ItemCardapioRepository;
import br.com.aftersunrise.easytable.shared.enums.PedidoStatus;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
public class PedidoAdapterImpl implements IPedidoAdapter {

    private final ItemCardapioRepository itemCardapioRepository;

public PedidoAdapterImpl(ItemCardapioRepository itemCardapioRepository) {
        this.itemCardapioRepository = itemCardapioRepository;
    }

    @Override
    public Pedido toPedido(CreatePedidoCommand request) {
        List<ItemCardapio> itens = itemCardapioRepository.findAllById(request.itensIds());
        if(itens.size() != request.itensIds().size()) {
            throw new IllegalArgumentException("Alguns itens não foram encontrados no cardápio");
        }

        return Pedido.builder()
                .mesaId(request.mesaId())
                .comandaId(request.comandaId())
                .itens(itens)
                .dataHora(new Date())
                .status(PedidoStatus.PENDENTE)
                .build();
    }

    @Override
    public void updatePedido(Pedido pedido, UpdateStatusPedidoCommand request) {
        if (request.status() != null) {
            pedido.setStatus(request.status());
        }
    }
}