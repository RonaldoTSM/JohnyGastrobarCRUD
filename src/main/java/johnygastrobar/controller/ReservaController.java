package johnygastrobar.controller;

import johnygastrobar.model.Reserva;
import johnygastrobar.service.ReservaService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reservas") // Caminho base para os endpoints de Reserva
public class ReservaController {

    private final ReservaService reservaService;

    @Autowired
    public ReservaController(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    @PostMapping
    public ResponseEntity<Reserva> criarReserva(@RequestBody Reserva reserva) {
        Reserva novaReserva = reservaService.criarReserva(reserva);
        return new ResponseEntity<>(novaReserva, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Reserva> buscarReservaPorId(@PathVariable int id) {
        Reserva reserva = reservaService.buscarReservaPorId(id);
        return ResponseEntity.ok(reserva);
    }

    @GetMapping
    public ResponseEntity<List<Reserva>> listarTodasReservas() {
        List<Reserva> reservas = reservaService.listarTodasReservas();
        return ResponseEntity.ok(reservas);
    }

    // Endpoint para listar reservas por data específica
    // Ex: GET /api/reservas/por-data?data=2025-12-31
    @GetMapping("/por-data")
    public ResponseEntity<List<Reserva>> listarReservasPorData(
            @RequestParam("data") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        List<Reserva> reservas = reservaService.listarReservasPorData(data);
        return ResponseEntity.ok(reservas);
    }


    @PutMapping("/{id}")
    public ResponseEntity<Reserva> atualizarReserva(@PathVariable int id, @RequestBody Reserva reserva) {
        reserva.setIdReserva(id); // Garante que o ID da reserva a ser atualizada é o da URL
        Reserva reservaAtualizada = reservaService.atualizarReserva(reserva);
        return ResponseEntity.ok(reservaAtualizada);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarReserva(@PathVariable int id) {
        reservaService.deletarReserva(id);
        return ResponseEntity.noContent().build();
    }
}