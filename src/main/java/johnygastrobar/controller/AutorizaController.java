package johnygastrobar.controller;

import johnygastrobar.model.Autoriza;
import johnygastrobar.service.AutorizaService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/autorizacoes")
public class AutorizaController {

    private final AutorizaService autorizaService;

    @Autowired
    public AutorizaController(AutorizaService autorizaService) {
        this.autorizaService = autorizaService;
    }

    @PostMapping
    public ResponseEntity<Autoriza> criarAutorizacao(@RequestBody Autoriza autoriza) {
        Autoriza novaAutorizacao = autorizaService.criarAutorizacao(autoriza);
        return new ResponseEntity<>(novaAutorizacao, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Autoriza> buscarAutorizacaoPorId(@PathVariable int id) {
        Autoriza autoriza = autorizaService.buscarAutorizacaoPorId(id);
        return ResponseEntity.ok(autoriza);
    }

    @GetMapping
    public ResponseEntity<List<Autoriza>> listarTodasAutorizacoes() {
        List<Autoriza> autorizacoes = autorizaService.listarTodasAutorizacoes();
        return ResponseEntity.ok(autorizacoes);
    }

    @GetMapping("/por-pedido/{idPedido}")
    public ResponseEntity<List<Autoriza>> listarAutorizacoesPorPedido(@PathVariable int idPedido) {
        List<Autoriza> autorizacoes = autorizaService.listarAutorizacoesPorPedido(idPedido);
        return ResponseEntity.ok(autorizacoes);
    }

    @GetMapping("/por-gerente/{idGerente}")
    public ResponseEntity<List<Autoriza>> listarAutorizacoesPorGerente(@PathVariable int idGerente) {
        List<Autoriza> autorizacoes = autorizaService.listarAutorizacoesPorGerente(idGerente);
        return ResponseEntity.ok(autorizacoes);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Autoriza> atualizarAutorizacao(@PathVariable int id, @RequestBody Autoriza autoriza) {
        autoriza.setIdAutorizacao(id);
        Autoriza autorizacaoAtualizada = autorizaService.atualizarAutorizacao(autoriza);
        return ResponseEntity.ok(autorizacaoAtualizada);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarAutorizacao(@PathVariable int id) {
        autorizaService.deletarAutorizacao(id);
        return ResponseEntity.noContent().build();
    }
}