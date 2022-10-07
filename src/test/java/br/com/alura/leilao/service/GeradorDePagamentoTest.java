package br.com.alura.leilao.service;

import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import br.com.alura.leilao.dao.PagamentoDao;
import br.com.alura.leilao.model.Lance;
import br.com.alura.leilao.model.Leilao;
import br.com.alura.leilao.model.Pagamento;
import br.com.alura.leilao.model.Usuario;

class GeradorDePagamentoTest {

	private GeradorDePagamento gerador;

	@Mock
	private PagamentoDao pagamentoDao;
	
	@Mock
	private Clock clock;
	
	@Captor
	private ArgumentCaptor<Pagamento> captor;

	@BeforeEach
	void beaforeEach() throws Exception {
		MockitoAnnotations.initMocks(this);
		this.gerador = new GeradorDePagamento(pagamentoDao, clock);
	}

	@Test
	void deveriaCriarPagamentoParaVencedorLeilao() {
		Leilao leilao = criarLeilao();
		Lance lanceVencedor = leilao.getLanceVencedor();
		
		LocalDate data = LocalDate.of(2020, 12, 7);
		
		Mockito.when(clock.instant()).
			thenReturn(data.atStartOfDay(ZoneId.systemDefault()).toInstant());
		Mockito.when(clock.getZone()).thenReturn(ZoneId.systemDefault());
		
		gerador.gerarPagamento(lanceVencedor);

		Mockito.verify(pagamentoDao).salvar(captor.capture());
		
		Pagamento pagamento = captor.getValue();
		
		assertEquals(data.plusDays(1), pagamento.getVencimento());
		assertEquals(lanceVencedor.getValor(), pagamento.getValor());
		assertFalse(pagamento.getPago());
		assertEquals(lanceVencedor.getUsuario(), pagamento.getUsuario());
		assertEquals(leilao, pagamento.getLeilao());
	}

	private Leilao criarLeilao() {
		Leilao leilao = new Leilao("Celular", new BigDecimal(500), new Usuario("Fulano"));

		Lance primeiro = new Lance(new Usuario("Fulano"), new BigDecimal(600));

		leilao.propoe(primeiro);
		leilao.setLanceVencedor(primeiro);

		return leilao;
	}
}
