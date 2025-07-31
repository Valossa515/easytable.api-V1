package br.com.aftersunrise.easytable.shared.helpers;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FileContentResult {
    private byte[] fileData;
    private String contentType;
    private String fileName;
}
