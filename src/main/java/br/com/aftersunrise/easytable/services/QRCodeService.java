package br.com.aftersunrise.easytable.services;

import net.glxn.qrgen.core.image.ImageType;
import net.glxn.qrgen.javase.QRCode;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
public class QRCodeService {
    public byte[] generateQRCode(String text) {
        ByteArrayOutputStream outputStream = QRCode.from(text)
                .withSize(300, 300)
                .to(ImageType.PNG)
                .stream();

        return outputStream.toByteArray();
    }
}
