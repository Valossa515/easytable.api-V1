package br.com.aftersunrise.easytable.builders;


import br.com.aftersunrise.easytable.borders.dtos.requests.CreatePedidoRequest;
import br.com.aftersunrise.easytable.shared.enums.PedidoStatus;

import java.util.Date;
import java.util.List;

public class CreatePedidoRequestBuilder {

    private String mesaId;
    private List<String> itensIds;
    private Date dataHora = new Date(); // default: agora
    private PedidoStatus status = PedidoStatus.PENDENTE; // default
    private String comandaId;
    private String qrCodeAcompanhamento;

    public static CreatePedidoRequestBuilder builder() {
        return new CreatePedidoRequestBuilder();
    }

    public CreatePedidoRequestBuilder mesaId(String mesaId) {
        this.mesaId = mesaId;
        return this;
    }

    public CreatePedidoRequestBuilder itensIds(List<String> itensIds) {
        this.itensIds = itensIds;
        return this;
    }

    public CreatePedidoRequestBuilder comandaId(String comandaId) {
        this.comandaId = comandaId;
        return this;
    }

    public CreatePedidoRequestBuilder dataHora(Date dataHora) {
        this.dataHora = dataHora;
        return this;
    }

    public CreatePedidoRequestBuilder status(PedidoStatus status) {
        this.status = status;
        return this;
    }

    public CreatePedidoRequestBuilder qrCodeAcompanhamento(String qrCodeAcompanhamento) {
        this.qrCodeAcompanhamento = qrCodeAcompanhamento;
        return this;
    }

    public CreatePedidoRequest toCreatePedidoRequest() {
        return new CreatePedidoRequest(
                mesaId,
                itensIds,
                dataHora,
                status,
                comandaId,
                qrCodeAcompanhamento
        );
    }
}
