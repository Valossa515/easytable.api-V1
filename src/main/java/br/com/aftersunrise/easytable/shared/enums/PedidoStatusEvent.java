package br.com.aftersunrise.easytable.shared.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum PedidoStatusEvent {
    INICIAR_PREPARO,
    CONCLUIR_PREPARO,
    ENTREGAR,
    SOLICITAR_TROCA,
    CONFIRMAR_PAGAMENTO,
    CANCELAR;

    @JsonCreator
    public static PedidoStatusEvent fromString(String value) {
        if (value == null) return null;
        for (PedidoStatusEvent event : PedidoStatusEvent.values()) {
            if (event.name().equalsIgnoreCase(value)) {
                return event;
            }
        }
        return null;
    }
}
