package br.com.aftersunrise.easytable.repositories;

import br.com.aftersunrise.easytable.borders.entities.ItemCardapio;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemCardapioRepository extends MongoRepository<ItemCardapio, String> {
    @Override
    List<ItemCardapio> findAllById(Iterable<String> ids);
}