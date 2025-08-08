package br.com.aftersunrise.easytable.borders.dtos.requests;

import br.com.aftersunrise.easytable.borders.dtos.responses.FechamentoResponse;
import br.com.aftersunrise.easytable.shared.handlers.ICommand;

public record FechamentoComandaCommand(String codigoQR) implements ICommand<FechamentoResponse> {
}
