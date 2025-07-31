package br.com.aftersunrise.easytable.borders.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "comandas")
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Comanda extends DatabaseEntityBase{
    private String codigoQR;
    private String mesaId;
    private boolean ativa;
    private Date dataCriacao;
    private byte[] qrCodeImagem;
}
