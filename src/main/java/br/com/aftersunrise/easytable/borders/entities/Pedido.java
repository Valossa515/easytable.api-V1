package br.com.aftersunrise.easytable.borders.entities;

import br.com.aftersunrise.easytable.shared.enums.PedidoStatus;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Document(collection = "pedidos")
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Pedido extends DatabaseEntityBase{
    private String mesaId;
    private String comandaId;
    @Builder.Default
    private List<ItemCardapio> itens = new ArrayList<>();
    private Date dataHora;
    private PedidoStatus status;
}
