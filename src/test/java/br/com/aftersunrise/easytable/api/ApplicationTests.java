package br.com.aftersunrise.easytable.api;

import br.com.aftersunrise.easytable.repositories.ComandaRepository;
import br.com.aftersunrise.easytable.repositories.ItemCardapioRepository;
import br.com.aftersunrise.easytable.repositories.MesaRepository;
import br.com.aftersunrise.easytable.repositories.PedidoRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class ApplicationTests {
	@MockBean
	private PedidoRepository pedidoRepository;

	@MockBean
	private ItemCardapioRepository itemCardapioRepository;

	@MockBean
	private MesaRepository mesaRepository;

	@MockBean
	private ComandaRepository comandaRepository;

	@Test

	void contextLoads() {
		final Logger logger = org.slf4j.LoggerFactory.getLogger(ApplicationTests.class);
		logger.info("Contexto da aplicação carregado com sucesso.");
	}
}
