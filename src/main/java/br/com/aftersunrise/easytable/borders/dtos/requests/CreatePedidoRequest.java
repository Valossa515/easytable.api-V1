package br.com.aftersunrise.easytable.borders.dtos.requests;

import br.com.aftersunrise.easytable.shared.enums.PedidoStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Date;
import java.util.List;

public record CreatePedidoRequest(
        String mesaId,
        List<String> itensIds,  // Apenas os IDs dos itens
        @JsonIgnore
        Date dataHora,
        @JsonIgnore
        PedidoStatus status,
        String comandaId,
        @JsonIgnore
        String qrCodeAcompanhamento
) { }
