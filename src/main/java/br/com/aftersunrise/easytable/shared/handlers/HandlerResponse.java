package br.com.aftersunrise.easytable.shared.handlers;

import br.com.aftersunrise.easytable.shared.models.Message;
import lombok.Data;

import java.util.Collections;
import java.util.List;

@Data
public class HandlerResponse {
    private int statusCode;
    private List<Message> messages = Collections.emptyList();
    public boolean isSuccess() {
        return statusCode >= 200 && statusCode <= 299;
    }
}
