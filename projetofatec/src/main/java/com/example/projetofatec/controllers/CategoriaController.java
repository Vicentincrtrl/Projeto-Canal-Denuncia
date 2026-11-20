package com.example.projetofatec.controllers;

// Imports
import com.example.projetofatec.entities.Categoria;
import com.example.projetofatec.repositories.CategoriaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

// Expõe os endpoints REST de gerenciamento de categorias em /categorias
@RestController
@RequestMapping("/categorias")
// A classe CategoriaController é responsável por gerenciar as operações relacionadas às categorias, como listar todas as categorias e criar uma nova categoria. 
// Ela utiliza o repositório CategoriaRepository para interagir com o banco de dados.
public class CategoriaController {
    private final CategoriaRepository categoriaRepository;
    // Injeção de dependência via construtor para manter o repositório imutável e testável
    public CategoriaController(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    // Rota GET para listar todas
    @GetMapping
    public List<Categoria> listar() {
        return categoriaRepository.findAll();
    }

    // Rota POST para criar uma nova categoria
    @PostMapping
    public ResponseEntity<Categoria> criar(@RequestBody Categoria categoria) {
        Categoria novaCategoria = categoriaRepository.save(categoria);
        return ResponseEntity.status(201).body(novaCategoria);
    }
}