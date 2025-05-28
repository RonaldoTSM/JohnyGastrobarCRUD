package johnygastrobar.controller;

import johnygastrobar.model.TopItemInfo; // Importe a classe auxiliar que criamos
import johnygastrobar.service.FeedbackPedidoService;
import johnygastrobar.service.ItemService;
import johnygastrobar.service.PagamentoService;
import johnygastrobar.service.PedidoService;
import johnygastrobar.service.ReservaService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final PagamentoService pagamentoService;
    private final PedidoService pedidoService;
    private final ItemService itemService;
    private final ReservaService reservaService;
    private final FeedbackPedidoService feedbackPedidoService;

    @Autowired
    public DashboardController(PagamentoService pagamentoService,
                               PedidoService pedidoService,
                               ItemService itemService,
                               ReservaService reservaService,
                               FeedbackPedidoService feedbackPedidoService) {
        this.pagamentoService = pagamentoService;
        this.pedidoService = pedidoService;
        this.itemService = itemService;
        this.reservaService = reservaService;
        this.feedbackPedidoService = feedbackPedidoService;
    }

    // --- MÉTRICAS FINANCEIRAS ---
    @GetMapping("/financeiro")
    public ResponseEntity<Map<String, Object>> getMetricasFinanceiras(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicial,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFinal) {

        BigDecimal faturamentoBruto = pagamentoService.getFaturamentoTotalPorPeriodo(dataInicial, dataFinal);
        int totalPedidosPagos = pedidoService.countPedidosPagosPorPeriodo(dataInicial, dataFinal);

        BigDecimal ticketMedio = BigDecimal.ZERO;
        if (totalPedidosPagos > 0 && faturamentoBruto.compareTo(BigDecimal.ZERO) > 0) {
            ticketMedio = faturamentoBruto.divide(new BigDecimal(totalPedidosPagos), 2, RoundingMode.HALF_UP);
        }

        Map<String, Object> metricas = new HashMap<>();
        metricas.put("faturamentoBrutoTotal", faturamentoBruto);
        metricas.put("ticketMedioPorPedido", ticketMedio);

        return ResponseEntity.ok(metricas);
    }

    // --- MÉTRICAS DE VENDAS E ITENS ---
    @GetMapping("/vendas/top-itens-mais-vendidos")
    public ResponseEntity<List<TopItemInfo>> getTopItensMaisVendidos(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicial,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFinal,
            @RequestParam(defaultValue = "5") int limite) {
        List<TopItemInfo> topItens = itemService.getTopItensMaisVendidosPorQuantidade(dataInicial, dataFinal, limite);
        return ResponseEntity.ok(topItens);
    }

    @GetMapping("/vendas/top-itens-mais-rentaveis")
    public ResponseEntity<List<TopItemInfo>> getTopItensMaisRentaveis(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicial,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFinal,
            @RequestParam(defaultValue = "5") int limite) {
        List<TopItemInfo> topItens = itemService.getTopItensMaisRentaveis(dataInicial, dataFinal, limite);
        return ResponseEntity.ok(topItens);
    }

    @GetMapping("/vendas/total-unidades-vendidas")
    public ResponseEntity<BigDecimal> getTotalUnidadesItensVendidos(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicial,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFinal) {
        BigDecimal totalUnidades = pedidoService.sumQuantidadeItensVendidosPorPeriodo(dataInicial, dataFinal);
        return ResponseEntity.ok(totalUnidades);
    }

    // --- MÉTRICAS OPERACIONAIS DE PEDIDOS ---
    @GetMapping("/pedidos/contagem-criados")
    public ResponseEntity<Integer> getTotalPedidosCriados(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicial,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFinal) {
        int totalPedidos = pedidoService.countPedidosCriadosPorPeriodo(dataInicial, dataFinal);
        return ResponseEntity.ok(totalPedidos);
    }

    @GetMapping("/pedidos/contagem-por-status")
    public ResponseEntity<Map<String, Integer>> getContagemPedidosPorStatus(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicial,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFinal) {
        Map<String, Integer> contagens = pedidoService.getContagemPedidosPorStatus(dataInicial, dataFinal);
        return ResponseEntity.ok(contagens);
    }

    // --- MÉTRICAS DE RESERVAS ---
    @GetMapping("/reservas/hoje")
    public ResponseEntity<Map<String, Integer>> getMetricasReservasHoje() {
        LocalDate hoje = LocalDate.now();
        int totalReservasHoje = reservaService.getTotalReservasParaData(hoje);
        int totalPessoasHoje = reservaService.getTotalPessoasEsperadasParaData(hoje);

        Map<String, Integer> metricas = new HashMap<>();
        metricas.put("totalReservasHoje", totalReservasHoje);
        metricas.put("totalPessoasEsperadasHoje", totalPessoasHoje);
        return ResponseEntity.ok(metricas);
    }

    @GetMapping("/reservas/amanha")
    public ResponseEntity<Map<String, Integer>> getMetricasReservasAmanha() {
        LocalDate amanha = LocalDate.now().plusDays(1);
        int totalReservasAmanha = reservaService.getTotalReservasParaData(amanha);
        int totalPessoasAmanha = reservaService.getTotalPessoasEsperadasParaData(amanha);

        Map<String, Integer> metricas = new HashMap<>();
        metricas.put("totalReservasAmanha", totalReservasAmanha);
        metricas.put("totalPessoasEsperadasAmanha", totalPessoasAmanha);
        return ResponseEntity.ok(metricas);
    }

    // --- MÉTRICAS DE QUALIDADE E SATISFAÇÃO ---
    @GetMapping("/qualidade/metricas-feedback")
    public ResponseEntity<Map<String, Object>> getMetricasFeedback(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicial,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFinal) {

        BigDecimal notaMediaComida = feedbackPedidoService.getNotaMediaComidaPorPeriodo(dataInicial, dataFinal);
        BigDecimal notaMediaAtendimento = feedbackPedidoService.getNotaMediaAtendimentoPorPeriodo(dataInicial, dataFinal);
        int totalFeedbacks = feedbackPedidoService.getTotalFeedbacksPorPeriodo(dataInicial, dataFinal);

        Map<String, Object> metricas = new HashMap<>();
        metricas.put("notaMediaComida", notaMediaComida); // Pode ser null se não houver dados
        metricas.put("notaMediaAtendimento", notaMediaAtendimento); // Pode ser null
        metricas.put("totalFeedbacksRecebidos", totalFeedbacks);

        return ResponseEntity.ok(metricas);
    }
}