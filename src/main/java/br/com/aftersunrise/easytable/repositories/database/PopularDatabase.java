package br.com.aftersunrise.easytable.repositories.database;

import br.com.aftersunrise.easytable.borders.entities.Comanda;
import br.com.aftersunrise.easytable.borders.entities.ItemCardapio;
import br.com.aftersunrise.easytable.borders.entities.Mesa;
import br.com.aftersunrise.easytable.configs.QrCodeProperties;
import br.com.aftersunrise.easytable.repositories.ComandaRepository;
import br.com.aftersunrise.easytable.repositories.ItemCardapioRepository;
import br.com.aftersunrise.easytable.repositories.MesaRepository;
import br.com.aftersunrise.easytable.services.QRCodeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Configuration
@Slf4j
public class PopularDatabase {

    final int numeroMesa = 20;
    private final QrCodeProperties qrCodeProperties;
    private final QRCodeService qrCodeService;

    public PopularDatabase(QrCodeProperties qrCodeProperties, QRCodeService qrCodeService) {
        this.qrCodeProperties = qrCodeProperties;
        this.qrCodeService = qrCodeService;
    }

    @Bean
    CommandLineRunner initializeDatabase(
            MesaRepository mesaRepository,
            ItemCardapioRepository itemCardapioRepository,
            ComandaRepository comandaRepository) { // Injetando todos os repositórios necessários
        return args -> {
            log.info("Iniciando processo de inicialização do banco de dados...");

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
                log.info("Mesas populadas com sucesso.");
            } else {
                log.info("Mesas já existentes. Pulando população de mesas.");
            }

            // --- 2. Popular Itens do Cardápio ---
            if (itemCardapioRepository.count() == 0) {
                log.info("Populando itens do cardápio...");
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

                log.info("Itens do cardápio populados com sucesso.");
            } else {
                log.info("Itens do cardápio já existentes. Pulando população de itens.");
            }

            // --- 3. Criar Diretório para QR Codes ---
            String qrCodeOutputDirectory = "qrcodes_para_impressao";
            Files.createDirectories(Paths.get(qrCodeOutputDirectory));
            log.info("Diretório para QR Codes criado/verificado: {}", qrCodeOutputDirectory);

            // --- 4. Criar Comandas e Gerar QR Codes para cada Mesa ---
            log.info("Iniciando criação/verificação de comandas e geração de QR Codes para impressão...");
            // Agora, mesaRepository.findAll() com certeza retornará mesas, pois elas foram populadas acima.
            mesaRepository.findAll().forEach(mesa -> {
                Optional<Comanda> comandaOptional = comandaRepository.findByMesaId(mesa.getId());
                Comanda comanda;

                if (comandaOptional.isEmpty()) { // Se a comanda não existe para esta mesa
                    comanda = Comanda.builder()
                            .codigoQR(UUID.randomUUID().toString())
                            .mesaId(mesa.getId())
                            .ativa(true)
                            .dataCriacao(new Date())
                            .build();
                    comandaRepository.save(comanda);
                    log.info("Comanda criada para a mesa {} com código: {}", mesa.getNumero(), comanda.getCodigoQR());
                } else { // Se a comanda já existe
                    comanda = comandaOptional.get();
                    log.info("Comanda existente para a mesa {} com código: {}", mesa.getNumero(), comanda.getCodigoQR());
                }

                String unifiedQrCodeContent = String.format(
                        "{\"comanda\":\"%s\", \"mesaId\":\"%s\", \"url_inicial\":\"%s/novo-pedido?comanda=%s\"}",
                        comanda.getCodigoQR(),
                        mesa.getId(),
                        qrCodeProperties.getBaseUrl(),
                        comanda.getCodigoQR()
                );

                try {
                    byte[] unifiedQrCodeImage = qrCodeService.generateQRCode(unifiedQrCodeContent);

                    String filename = "qrcode_comanda_mesa_" + mesa.getNumero() + "_" + comanda.getId() + ".png";
                    String outputPath = Paths.get(qrCodeOutputDirectory, filename).toString();

                    try (FileOutputStream fos = new FileOutputStream(outputPath)) {
                        fos.write(unifiedQrCodeImage);
                    }
                    log.info("QR Code unificado gerado e salvo para impressão em: {}", outputPath);

                    // Opcional: Salvar o caminho do arquivo no objeto Comanda
                    // para facilitar a recuperação posterior, se necessário.
                    // comanda.setQrCodeImagePath(outputPath);
                    // comandaRepository.save(comanda);

                } catch (Exception e) {
                    log.info("Erro ao gerar ou salvar QR Code para a mesa {}: {}", mesa.getNumero(), e.getMessage());
                }
            });
            log.info("Processo de inicialização do banco de dados e geração de QR Codes finalizado.");
        };
    }
}
