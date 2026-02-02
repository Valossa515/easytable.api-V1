package br.com.aftersunrise.easytable.configs;

import br.com.aftersunrise.easytable.shared.enums.PedidoStatusEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new StringToPedidoStatusEventConverter());
    }

    private static class StringToPedidoStatusEventConverter implements Converter<String, PedidoStatusEvent> {
        @Override
        public PedidoStatusEvent convert(String source) {
            return PedidoStatusEvent.fromString(source);
        }
    }
}
