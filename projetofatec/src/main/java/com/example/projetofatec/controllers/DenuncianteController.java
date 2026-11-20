package com.example.projetofatec.controllers;

// Imports
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.projetofatec.entities.Denuncia;
import com.example.projetofatec.services.DenunciantesService;
import lombok.RequiredArgsConstructor;

// A classe DenuncianteController é responsável por gerenciar as operações relacionadas aos denunciantes anônimos, 
// como listar todos os denunciantes, buscar um denunciante pelo ID e criar um novo ID anônimo para quem for fazer a denúncia.
@RestController
@RequestMapping("/denunciantes")
@RequiredArgsConstructor
// Libera o acesso para o Front-end (evita bloqueio de CORS)
public class DenuncianteController {
    
    private final DenunciantesService denuncianteService;

    // GET: Listar todos os denunciantes anônimos
    @GetMapping
    public ResponseEntity<List<Denuncia>> listarTodos() {
        return ResponseEntity.ok(denuncianteService.listarTodos());
    }

    // GET por ID: Buscar um denunciante pelo ID
    @GetMapping("/{id}")
    public ResponseEntity<Denuncia> buscarPorId(@PathVariable Long id) {
        Denuncia denuncia = denuncianteService.buscarPorId(id);
        if (denuncia != null) {
            return ResponseEntity.ok(denuncia);
        }
        return ResponseEntity.notFound().build();
    }

    // POST: Criar um novo ID anônimo para quem for fazer a denúncia
    @PostMapping
    public ResponseEntity<Denuncia> incluir() {
        Denuncia novo = denuncianteService.incluir(new Denuncia());
        return ResponseEntity.status(HttpStatus.CREATED).body(novo);
    }
}