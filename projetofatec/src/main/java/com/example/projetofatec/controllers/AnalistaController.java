package com.example.projetofatec.controllers;
//Imports
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.projetofatec.entities.Analista;
import com.example.projetofatec.services.AnalistaService;

// Expõe os endpoints REST de gerenciamento de analistas em /analistas
@RestController
@RequestMapping("/analistas")
public class AnalistaController {

    // Injeção de dependência via construtor para manter o serviço imutável e testável
    private final AnalistaService analistaService;

    public AnalistaController(AnalistaService analistaService) {
        this.analistaService = analistaService;
    }
    // Retorna todos os analistas cadastrados
    @GetMapping
    public ResponseEntity<List<Analista>> listarTodos() {
        return ResponseEntity.ok(analistaService.listarTodos());
    }
    // Busca um analista específico pelo ID informado na URL
    @GetMapping("/{id}")
    public ResponseEntity<Analista> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(analistaService.buscarPorId(id));
    }
    //  Cadastra um novo analista e retorna HTTP 201 (Created) com o registro criado
    @PostMapping
    public ResponseEntity<Analista> incluir(@RequestBody Analista analista) {
        Analista novo = analistaService.incluir(analista);
        return ResponseEntity.status(HttpStatus.CREATED).body(novo);
    }
    // Atualiza os dados do ID informado; devolve 404 (Not Found) se o registro não existir
    @PutMapping("/{id}")
    public ResponseEntity<Analista> editar(@PathVariable Long id, @RequestBody Analista analista) {
        Analista atualizado = analistaService.editar(id, analista);
        if (atualizado != null) {
            return ResponseEntity.ok(atualizado);
        }
        return ResponseEntity.notFound().build();
    }
    // Remove o analista pelo ID e retorna HTTP 204 (No Content) em caso de sucesso
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        analistaService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}