package br.com.aftersunrise.easytable.borders.dtos.requests;

import br.com.aftersunrise.easytable.handlers.validators.annotations.ValidCreatePedido;
import br.com.aftersunrise.easytable.shared.enums.PedidoStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Date;
import java.util.List;

@ValidCreatePedido
public record CreatePedidoRequest(
        String mesaId,
        List<String> itensIds,
        @JsonIgnore
        Date dataHora,
        @JsonIgnore
        PedidoStatus status,
        String comandaId,
        @JsonIgnore
        String qrCodeAcompanhamento
) { }
