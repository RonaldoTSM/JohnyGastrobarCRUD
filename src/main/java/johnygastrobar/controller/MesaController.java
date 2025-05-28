package johnygastrobar.controller;

import johnygastrobar.model.Mesa;
import johnygastrobar.service.MesaService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mesas") // Caminho base para os endpoints de Mesa
public class MesaController {

    private final MesaService mesaService;

    @Autowired
    public MesaController(MesaService mesaService) {
        this.mesaService = mesaService;
    }

    @PostMapping
    public ResponseEntity<Mesa> criarMesa(@RequestBody Mesa mesa) {
        Mesa novaMesa = mesaService.criarMesa(mesa);
        return new ResponseEntity<>(novaMesa, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Mesa> buscarMesaPorId(@PathVariable int id) {
        Mesa mesa = mesaService.buscarMesaPorId(id);
        return ResponseEntity.ok(mesa);
    }

    @GetMapping
    public ResponseEntity<List<Mesa>> listarTodasMesas() {
        List<Mesa> mesas = mesaService.listarTodasMesas();
        return ResponseEntity.ok(mesas);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Mesa> atualizarMesa(@PathVariable int id, @RequestBody Mesa mesa) {
        mesa.setIdMesa(id); // Garante que o ID da mesa a ser atualizada é o da URL
        Mesa mesaAtualizada = mesaService.atualizarMesa(mesa);
        return ResponseEntity.ok(mesaAtualizada);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarMesa(@PathVariable int id) {
        mesaService.deletarMesa(id);
        return ResponseEntity.noContent().build();
    }
}