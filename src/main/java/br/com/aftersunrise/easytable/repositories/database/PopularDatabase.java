package br.com.aftersunrise.easytable.repositories.database;

import br.com.aftersunrise.easytable.borders.entities.ItemCardapio;
import br.com.aftersunrise.easytable.borders.entities.Mesa;
import br.com.aftersunrise.easytable.repositories.ComandaRepository;
import br.com.aftersunrise.easytable.repositories.ItemCardapioRepository;
import br.com.aftersunrise.easytable.repositories.MesaRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PopularDatabase {

    final int numeroMesa = 20;

    @Bean
    CommandLineRunner initializeDatabase(
            ComandaRepository comandaRepository,
            ItemCardapioRepository itemCardapioRepository,
            MesaRepository mesaRepository) {
        return args -> {
            System.out.println("Iniciando processo de inicialização do banco de dados...");

            // --- 1. Popular Mesas ---
            if (mesaRepository.count() == 0) {
                System.out.println("Populando mesas...");
                for (int i = 1; i <= numeroMesa; i++) {
                    Mesa mesa = Mesa.builder()
                            .numero(i)
                            .ativa(true)
                            .build();
                    mesaRepository.save(mesa);
                }
                System.out.println("Mesas populadas com sucesso.");
            } else {
                System.out.println("Mesas já existentes. Pulando população de mesas.");
            }

            // --- 2. Popular Itens do Cardápio ---
            if (itemCardapioRepository.count() == 0) {
                System.out.println("Populando itens do cardápio...");
                itemCardapioRepository.save(ItemCardapio.builder()
                        .nome("Hambúrguer Artesanal")
                        .descricao("Pão brioche, hambúrguer 180g, queijo cheddar, bacon e molho especial.")
                        .preco(28.90)
                        .imagemUrl("https://example.com/img/hamburguer.jpg")
                        .build());

                itemCardapioRepository.save(ItemCardapio.builder()
                        .nome("Batata Frita")
                        .descricao("Batata frita crocante com toque de páprica.")
                        .preco(12.00)
                        .imagemUrl("https://example.com/img/batata.jpg")
                        .build());

                itemCardapioRepository.save(ItemCardapio.builder()
                        .nome("Refrigerante Lata")
                        .descricao("350ml - Coca-Cola, Guaraná ou Fanta.")
                        .preco(6.00)
                        .imagemUrl("https://example.com/img/refrigerante.jpg")
                        .build());

                itemCardapioRepository.save(ItemCardapio.builder()
                        .nome("Suco Natural")
                        .descricao("Suco natural de laranja ou limão.")
                        .preco(8.50)
                        .imagemUrl("https://example.com/img/suco.jpg")
                        .build());

                itemCardapioRepository.save(ItemCardapio.builder()
                        .nome("Salada Caesar")
                        .descricao("Alface romana, frango grelhado, parmesão e croutons.")
                        .preco(22.00)
                        .imagemUrl("https://example.com/img/salada.jpg")
                        .build());

                System.out.println("Itens do cardápio populados com sucesso.");
            } else {
                System.out.println("Itens do cardápio já existentes. Pulando população de itens.");
            }
        };
    }
}
