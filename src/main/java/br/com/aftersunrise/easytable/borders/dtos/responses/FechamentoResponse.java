package br.com.aftersunrise.easytable.borders.dtos.responses;

import java.math.BigDecimal;

public record FechamentoResponse
        (String comandaId,
         BigDecimal total,
         String mensagem) { }
