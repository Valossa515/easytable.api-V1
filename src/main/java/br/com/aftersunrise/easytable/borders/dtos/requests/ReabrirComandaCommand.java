package br.com.aftersunrise.easytable.borders.dtos.requests;

import br.com.aftersunrise.easytable.borders.dtos.responses.ComandaResponse;
import br.com.aftersunrise.easytable.shared.handlers.ICommand;

public record ReabrirComandaCommand(String codigoQR) implements ICommand<ComandaResponse> {
}
