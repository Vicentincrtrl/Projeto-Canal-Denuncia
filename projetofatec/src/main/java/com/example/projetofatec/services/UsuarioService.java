package com.example.projetofatec.services;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Service;
import com.example.projetofatec.entities.Usuario;
import com.example.projetofatec.repositories.UsuarioRepository;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    // 1. Listar todos os usuários
    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    // 2. Buscar usuário pelo ID
    public Usuario buscarPorId(Long id) {
        Objects.requireNonNull(id, "ID do usuário não pode ser nulo");
        Optional<Usuario> usuario = usuarioRepository.findById(id);
        return usuario.orElseThrow(() -> new RuntimeException("Usuário não encontrado com o ID: " + id));
    }

    // 3. Salvar / Incluir novo usuário (ou Analista/Administrador)
    public Usuario incluir(Usuario usuario) {
        Objects.requireNonNull(usuario, "Usuário não pode ser nulo");
        usuario.setId(null); // Garante INSERT e evita sobrescrever ID existente
        return usuarioRepository.save(usuario);
    }

    // 4. Editar / Atualizar usuário existente
    public Usuario editar(Long id, Usuario usuarioAtualizado) {
        Objects.requireNonNull(id, "ID do usuário não pode ser nulo");
        Objects.requireNonNull(usuarioAtualizado, "Dados de atualização não podem ser nulos");

        Optional<Usuario> usuarioExistente = usuarioRepository.findById(id);

        if (usuarioExistente.isPresent()) {
            Usuario usuario = usuarioExistente.get();
            usuario.setNome(usuarioAtualizado.getNome());
            usuario.setEmail(usuarioAtualizado.getEmail());
            usuario.setSenha(usuarioAtualizado.getSenha());
            usuario.setAtivo(usuarioAtualizado.getAtivo());
            usuario.setPerfil(usuarioAtualizado.getPerfil());

            return usuarioRepository.save(usuario);
        }

        return null; // Retorna null para o Controller responder com 404 NOT FOUND
    }

    // 5. Deletar o usuário pelo ID
    public void deletar(Long id) {
        Objects.requireNonNull(id, "ID do usuário não pode ser nulo");
        if (usuarioRepository.existsById(id)) {
            usuarioRepository.deleteById(id);
        }
    }
}