package br.com.aftersunrise.easytable.borders.dtos.requests;

import br.com.aftersunrise.easytable.borders.dtos.responses.CreatePedidoResponse;
import br.com.aftersunrise.easytable.handlers.validators.annotations.ValidCreatePedido;
import br.com.aftersunrise.easytable.shared.handlers.ICommand;

import java.util.List;

@ValidCreatePedido
public record CreatePedidoCommand(
        String mesaId,
         List<String> itensIds,
         String comandaId) implements ICommand<CreatePedidoResponse> { }
