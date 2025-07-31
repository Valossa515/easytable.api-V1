package br.com.aftersunrise.easytable.shared.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PedidoStatus {
    PENDENTE,
    EM_PREPARACAO,
    PRONTO,
    ENTREGUE,
    CANCELADO;
}
