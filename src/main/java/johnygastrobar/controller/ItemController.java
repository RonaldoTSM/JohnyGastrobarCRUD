package johnygastrobar.controller;

import johnygastrobar.model.Item;
import johnygastrobar.service.ItemService;
// As exceções são tratadas pelo GlobalExceptionHandler

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/itens") // Caminho base para os endpoints de Item
public class ItemController {

    private final ItemService itemService;

    @Autowired
    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @PostMapping
    public ResponseEntity<Item> criarItem(@RequestBody Item item) {
        // Validações de @RequestBody (como @Valid) podem ser adicionadas aqui com DTOs no futuro.
        // Por agora, o serviço faz as validações de negócio.
        Item novoItem = itemService.criarItem(item);
        return new ResponseEntity<>(novoItem, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Item> buscarItemPorId(@PathVariable int id) {
        Item item = itemService.buscarItemPorId(id);
        return ResponseEntity.ok(item);
    }

    @GetMapping
    public ResponseEntity<List<Item>> listarTodosItens() {
        List<Item> itens = itemService.listarTodosItens();
        return ResponseEntity.ok(itens);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Item> atualizarItem(@PathVariable int id, @RequestBody Item item) {
        // Garante que o ID do item a ser atualizado é o da URL, não o do corpo (se houver).
        // O serviço pode fazer uma checagem mais robusta se o ID do corpo for diferente e não zero.
        item.setIdItem(id);
        Item itemAtualizado = itemService.atualizarItem(item);
        return ResponseEntity.ok(itemAtualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarItem(@PathVariable int id) {
        itemService.deletarItem(id);
        return ResponseEntity.noContent().build();
    }
}