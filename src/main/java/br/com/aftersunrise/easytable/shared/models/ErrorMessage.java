package br.com.aftersunrise.easytable.shared.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorMessage {
    private String code = "000";
    private String message;

    public ErrorMessage(String message) {
        this.message = message;
    }
}
