package br.com.aftersunrise.easytable.repositories;

import br.com.aftersunrise.easytable.borders.entities.Mesa;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MesaRepository extends MongoRepository<Mesa, String> {

}
