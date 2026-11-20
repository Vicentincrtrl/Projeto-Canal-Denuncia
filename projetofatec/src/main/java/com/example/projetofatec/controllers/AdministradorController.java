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
import com.example.projetofatec.entities.Administrador;
import com.example.projetofatec.services.AdministradorService;

// Expõe os endpoints REST de gerenciamento de administradores em /administradores
@RestController
@RequestMapping("/administradores")
public class AdministradorController {

    private final AdministradorService administradorService;

    // Injeção de dependência via construtor para manter o serviço imutável e testável
    public AdministradorController(AdministradorService administradorService) {
        this.administradorService = administradorService;
    }

    // Retorna todos os administradores cadastrados
    @GetMapping
    public ResponseEntity<List<Administrador>> listarTodos() {
        return ResponseEntity.ok(administradorService.listarTodos());
    }

    // Busca um administrador específico pelo ID informado na URL
    @GetMapping("/{id}")
    public ResponseEntity<Administrador> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(administradorService.buscarPorId(id));
    }

    // Cadastra um novo administrador e retorna HTTP 201 (Created) com o registro criado
    @PostMapping
    public ResponseEntity<Administrador> incluir(@RequestBody Administrador administrador) {
        Administrador novo = administradorService.incluir(administrador);
        return ResponseEntity.status(HttpStatus.CREATED).body(novo);
    }

    // Atualiza os dados do ID informado; devolve 404 (Not Found) se o registro não existir
    @PutMapping("/{id}")
    public ResponseEntity<Administrador> editar(@PathVariable Long id, @RequestBody Administrador administrador) {
        Administrador atualizado = administradorService.editar(id, administrador);
        if (atualizado != null) {
            return ResponseEntity.ok(atualizado);
        }
        return ResponseEntity.notFound().build();
    }

    // Remove o administrador pelo ID e retorna HTTP 204 (No Content) em caso de sucesso
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        administradorService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}