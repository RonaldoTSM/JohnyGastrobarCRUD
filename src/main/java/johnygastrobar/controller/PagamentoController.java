package johnygastrobar.controller;

import johnygastrobar.model.Pagamento;
import johnygastrobar.service.PagamentoService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pagamentos")
public class PagamentoController {

    private final PagamentoService pagamentoService;

    @Autowired
    public PagamentoController(PagamentoService pagamentoService) {
        this.pagamentoService = pagamentoService;
    }

    @PostMapping
    public ResponseEntity<Pagamento> registrarPagamento(@RequestBody Pagamento pagamento) {
        // O PagamentoService.registrarPagamento já lida com a criação do pagamento
        // e a atualização do status do pedido relacionado (marcando-o como pago).
        Pagamento novoPagamento = pagamentoService.registrarPagamento(pagamento);
        return new ResponseEntity<>(novoPagamento, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Pagamento> buscarPagamentoPorId(@PathVariable int id) {
        Pagamento pagamento = pagamentoService.buscarPagamentoPorId(id);
        return ResponseEntity.ok(pagamento);
    }

    @GetMapping("/por-pedido/{idPedido}")
    public ResponseEntity<Pagamento> buscarPagamentoPorIdPedido(@PathVariable int idPedido) {
        Pagamento pagamento = pagamentoService.buscarPagamentoPorIdPedido(idPedido);
        return ResponseEntity.ok(pagamento);
    }

    @GetMapping
    public ResponseEntity<List<Pagamento>> listarTodosPagamentos() {
        List<Pagamento> pagamentos = pagamentoService.listarTodosPagamentos();
        return ResponseEntity.ok(pagamentos);
    }
}