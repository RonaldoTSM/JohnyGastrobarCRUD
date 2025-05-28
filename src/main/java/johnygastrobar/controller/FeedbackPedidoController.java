package johnygastrobar.controller;

import johnygastrobar.model.FeedbackPedido;
import johnygastrobar.service.FeedbackPedidoService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/feedbacks")
public class FeedbackPedidoController {

    private final FeedbackPedidoService feedbackPedidoService;

    @Autowired
    public FeedbackPedidoController(FeedbackPedidoService feedbackPedidoService) {
        this.feedbackPedidoService = feedbackPedidoService;
    }

    @PostMapping
    public ResponseEntity<FeedbackPedido> criarFeedback(@RequestBody FeedbackPedido feedback) {
        FeedbackPedido novoFeedback = feedbackPedidoService.criarFeedback(feedback);
        return new ResponseEntity<>(novoFeedback, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FeedbackPedido> buscarFeedbackPorId(@PathVariable int id) {
        FeedbackPedido feedback = feedbackPedidoService.buscarFeedbackPorId(id);
        return ResponseEntity.ok(feedback);
    }

    @GetMapping
    public ResponseEntity<List<FeedbackPedido>> listarTodosFeedbacks() {
        List<FeedbackPedido> feedbacks = feedbackPedidoService.listarTodosFeedbacks();
        return ResponseEntity.ok(feedbacks);
    }

    @GetMapping("/por-pedido/{idPedido}")
    public ResponseEntity<List<FeedbackPedido>> listarFeedbacksPorPedido(@PathVariable int idPedido) {
        List<FeedbackPedido> feedbacks = feedbackPedidoService.listarFeedbacksPorPedido(idPedido);
        return ResponseEntity.ok(feedbacks);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FeedbackPedido> atualizarFeedback(@PathVariable int id, @RequestBody FeedbackPedido feedback) {
        feedback.setIdFeedback(id);
        FeedbackPedido feedbackAtualizado = feedbackPedidoService.atualizarFeedback(feedback);
        return ResponseEntity.ok(feedbackAtualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarFeedback(@PathVariable int id) {
        feedbackPedidoService.deletarFeedback(id);
        return ResponseEntity.noContent().build();
    }
}