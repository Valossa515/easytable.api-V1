package br.com.aftersunrise.easytable.repositories;

import br.com.aftersunrise.easytable.borders.entities.Pedido;
import br.com.aftersunrise.easytable.shared.enums.PedidoStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PedidoRepository extends MongoRepository<Pedido, String> {
    List<Pedido> findByComandaId(String comandaId);
    List<Pedido> findByComandaIdAndStatus(String comandaId, PedidoStatus status);
}
