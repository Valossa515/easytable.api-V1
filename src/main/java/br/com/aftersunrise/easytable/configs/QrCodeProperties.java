package br.com.aftersunrise.easytable.configs;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "easytable.qrcode")
@Getter
@Setter
public class QrCodeProperties {
    private String baseUrl;
    private String statusPath;
    private String contaPath;
    private String acompanhamentoPath;
}
