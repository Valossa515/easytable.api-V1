package br.com.aftersunrise.easytable.borders.dtos.responses;

import java.math.BigDecimal;
import java.util.List;

public record FechamentoResponse
        (String comandaId,
         List<PedidoResponse> pedidos,
         BigDecimal total,
         String mensagem) { }
