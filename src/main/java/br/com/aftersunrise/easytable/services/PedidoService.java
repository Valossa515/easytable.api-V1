package br.com.aftersunrise.easytable.services;

import br.com.aftersunrise.easytable.borders.dtos.responses.FechamentoResponse;
import br.com.aftersunrise.easytable.borders.entities.Comanda;
import br.com.aftersunrise.easytable.borders.entities.ItemCardapio;
import br.com.aftersunrise.easytable.borders.entities.Pedido;
import br.com.aftersunrise.easytable.repositories.ComandaRepository;
import br.com.aftersunrise.easytable.repositories.PedidoRepository;
import br.com.aftersunrise.easytable.shared.enums.PedidoStatus;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PedidoService {
    private static final String PEDIDO_CACHE_PREFIX = "pedido:";
    private static final String COMANDA_PEDIDOS_CACHE_PREFIX = "comanda_pedidos:";

    private final PedidoRepository pedidoRepository;
    private final ComandaRepository comandaRepository;
    private final RedisService redisService;

    public CompletableFuture<PedidoStatus> getStatus(String id) {
        return CompletableFuture.supplyAsync(() -> obterPedido(id).getStatus());
    }

    public CompletableFuture<BigDecimal> calcularValorTotal(String pedidoId) {
        return CompletableFuture.supplyAsync(() -> {
            Pedido pedido = obterPedido(pedidoId);
            return calcularTotalDosItens(pedido);
        });
    }

    public CompletableFuture<BigDecimal> calcularValorTotalPorComanda(String comandaId) {
        return CompletableFuture.supplyAsync(() -> {
            List<Pedido> pedidos = buscarPedidosPorComanda(comandaId).join();
            return pedidos.stream()
                    .map(this::calcularTotalDosItens)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        });
    }

    public CompletableFuture<List<Pedido>> buscarPedidosPorComanda(String comandaId) {
        return CompletableFuture.supplyAsync(() -> {
            String cacheKey = COMANDA_PEDIDOS_CACHE_PREFIX + comandaId;

            // Buscar a lista diretamente do Redis (ajuste para seu método buscar que aceita TypeReference)
            List<Pedido> pedidos = redisService.buscar(cacheKey, new TypeReference<List<Pedido>>() {});

            if (pedidos == null || pedidos.isEmpty()) {
                pedidos = pedidoRepository.findByComandaId(comandaId);
                redisService.salvar(cacheKey, pedidos, 60); // Cache por 1 minuto
            }

            return pedidos;
        });
    }

    public CompletableFuture<Comanda> buscarComandaPorPedido(String pedidoId) {
        return CompletableFuture.supplyAsync(() -> {
            Pedido pedido = obterPedido(pedidoId);
            return comandaRepository.findById(pedido.getComandaId())
                    .orElseThrow(() -> new RuntimeException("Comanda não encontrada para o pedido: " + pedidoId));
        });
    }

    // Métodos auxiliares privados
    private Pedido obterPedido(String id) {
        String redisKey = PEDIDO_CACHE_PREFIX + id;
        Pedido pedido = redisService.buscar(redisKey, Pedido.class);

        return pedido != null
                ? pedido
                : pedidoRepository.findById(id).orElseThrow(() -> new RuntimeException("Pedido não encontrado: " + id));
    }

    private BigDecimal calcularTotalDosItens(Pedido pedido) {
        return contarItensPorId(pedido).entrySet().stream()
                .map(entry -> calcularSubtotal(entry.getKey(), entry.getValue(), pedido))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Map<String, Long> contarItensPorId(Pedido pedido) {
        return pedido.getItens().stream()
                .collect(Collectors.groupingBy(ItemCardapio::getId, Collectors.counting()));
    }

    private BigDecimal calcularSubtotal(String itemId, long quantidade, Pedido pedido) {
        ItemCardapio item = buscarItemNoPedido(itemId, pedido);
        return BigDecimal.valueOf(item.getPreco()).multiply(BigDecimal.valueOf(quantidade));
    }

    private ItemCardapio buscarItemNoPedido(String itemId, Pedido pedido) {
        return pedido.getItens().stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Item não encontrado no pedido: " + itemId));
    }

    public CompletableFuture<List<Pedido>> getPedidosPorComanda(String codigoQR) {
        return CompletableFuture.supplyAsync(() -> {
            Comanda comanda = comandaRepository.findByCodigoQR(codigoQR)
                    .orElseThrow(() -> new IllegalArgumentException("Comanda não encontrada"));

            String cacheKey = "comanda_pedidos:" + comanda.getId();
            List<Pedido> pedidos = redisService.buscar(cacheKey, new TypeReference<List<Pedido>>() {});
            if (pedidos == null || pedidos.isEmpty()) {
                pedidos = pedidoRepository.findByComandaId(comanda.getId());
                redisService.salvar(cacheKey, pedidos, 60);
            }

            return pedidos;
        });
    }

    public CompletableFuture<BigDecimal> getTotalPorComanda(String codigoQR) {
        return getPedidosPorComanda(codigoQR)
                .thenApply(pedidos -> pedidos.stream()
                        .map(this::calcularTotalDosItens)
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                );
    }

    public CompletableFuture<Map<PedidoStatus, Long>> getStatusPedidosPorComanda(String codigoQR) {
        return getPedidosPorComanda(codigoQR)
                .thenApply(pedidos -> pedidos.stream()
                        .collect(Collectors.groupingBy(
                                Pedido::getStatus,
                                Collectors.counting()
                        ))
                );
    }

    public CompletableFuture<FechamentoResponse> fecharContaPorComanda(String codigoQR) {
        return CompletableFuture.supplyAsync(() -> {
            // 1. Validar a comanda (exceção se não achar ou inativa)
            Comanda comanda = comandaRepository.findByCodigoQR(codigoQR)
                    .orElseThrow(() -> new RuntimeException("Comanda não encontrada"));

            if (!comanda.isAtiva()) {
                throw new RuntimeException("Comanda já está fechada");
            }

            // 2. Buscar pedidos da comanda
            List<Pedido> pedidos = pedidoRepository.findByComandaId(comanda.getId());

            // 3. Calcular total da conta
            BigDecimal total = pedidos.stream()
                    .map(this::calcularTotalDosItens)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // 4. Marcar comanda como inativa (fechada)
            comanda.setAtiva(false);
            comandaRepository.save(comanda);

            // 5. Limpar cache
            String cacheKey = COMANDA_PEDIDOS_CACHE_PREFIX + comanda.getId();
            redisService.deletar(cacheKey);

            // 6. Retornar resposta com total e info da comanda
            return new FechamentoResponse(comanda.getId(), total, "Conta fechada com sucesso");
        });
    }


    public CompletableFuture<Comanda> getComandaInfo(String codigoQR) {
        return CompletableFuture.supplyAsync(() ->
                comandaRepository.findByCodigoQR(codigoQR)
                        .orElseThrow(() -> new RuntimeException("Comanda não encontrada"))
        );
    }

    public CompletableFuture<Comanda> reabrirComanda(String codigoQR) {
        return CompletableFuture.supplyAsync(() -> {
            Comanda comanda = comandaRepository.findByCodigoQR(codigoQR)
                    .orElseThrow(() -> new RuntimeException("Comanda não encontrada"));

            if (comanda.isAtiva()) {
                throw new RuntimeException("Comanda já está ativa");
            }

            comanda.setAtiva(true);
            return comandaRepository.save(comanda);
        });
    }
}
