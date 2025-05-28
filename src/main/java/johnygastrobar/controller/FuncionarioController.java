package johnygastrobar.controller;

import johnygastrobar.model.Funcionario;
import johnygastrobar.service.FuncionarioService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/funcionarios")
public class FuncionarioController {

    private final FuncionarioService funcionarioService;

    @Autowired
    public FuncionarioController(FuncionarioService funcionarioService) {
        this.funcionarioService = funcionarioService;
    }

    @PostMapping
    public ResponseEntity<Funcionario> criarFuncionario(@RequestBody Funcionario funcionario) {
        // A lógica de try-catch foi movida para o GlobalExceptionHandler
        // O serviço lançará ServiceException ou IllegalArgumentException que serão tratadas globalmente.
        Funcionario novoFuncionario = funcionarioService.criarFuncionario(funcionario);
        return new ResponseEntity<>(novoFuncionario, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Funcionario> buscarFuncionarioPorId(@PathVariable int id) {
        // A lógica de try-catch para ResourceNotFoundException e ServiceException
        // foi movida para o GlobalExceptionHandler.
        Funcionario funcionario = funcionarioService.buscarFuncionarioPorId(id);
        return ResponseEntity.ok(funcionario);
    }

    @GetMapping
    public ResponseEntity<List<Funcionario>> listarTodosFuncionarios() {
        // A lógica de try-catch para ServiceException foi movida.
        List<Funcionario> funcionarios = funcionarioService.listarTodosFuncionarios();
        return ResponseEntity.ok(funcionarios);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Funcionario> atualizarFuncionario(@PathVariable int id, @RequestBody Funcionario funcionario) {
        if (funcionario.getId() != 0 && funcionario.getId() != id) {

            System.err.println("Conflito de ID na atualização: ID da URL=" + id + ", ID do corpo=" + funcionario.getId() + ". Usando ID da URL.");
        }
        funcionario.setId(id);

        Funcionario funcionarioAtualizado = funcionarioService.atualizarFuncionario(funcionario);
        return ResponseEntity.ok(funcionarioAtualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarFuncionario(@PathVariable int id) {
        funcionarioService.deletarFuncionario(id);
        return ResponseEntity.noContent().build();
    }
}