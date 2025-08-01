package br.com.aftersunrise.easytable.shared.models;

import lombok.Data;
import lombok.Getter;

@Data
public class Message {
    private String code;
    private String text;

    public Message(String code, String text) {
        this.code = code;
        this.text = text;
    }
}