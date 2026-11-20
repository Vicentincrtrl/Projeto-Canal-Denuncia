package com.example.projetofatec.controllers;

// Imports
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.projetofatec.entities.Usuario;
import com.example.projetofatec.services.UsuarioService;
import lombok.RequiredArgsConstructor;

// A classe UsuarioController é responsável por gerenciar as operações relacionadas aos usuários (Analistas e Administradores),
@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
// Libera o acesso para o Front-end (evita bloqueio de CORS)
public class UsuarioController {

    private final UsuarioService usuarioService;

    // Listar todos os usuários (Analistas e Administradores juntos no GET)
    @GetMapping
    public ResponseEntity<List<Usuario>> listarTodos() {
        return ResponseEntity.ok(usuarioService.listarTodos());
    }

    // Buscar por ID
    @GetMapping("/{id}")
    public ResponseEntity<Usuario> buscarPorId(@PathVariable Long id) {
        Usuario usuario = usuarioService.buscarPorId(id);
        if (usuario != null) {
            return ResponseEntity.ok(usuario);
        }
        return ResponseEntity.notFound().build();
    }
}