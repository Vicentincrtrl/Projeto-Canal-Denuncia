package com.example.projetofatec.services;

// Imports
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Service;
import com.example.projetofatec.entities.Administrador;
import com.example.projetofatec.repositories.AdministradorRepository;

@Service
// A classe AdministradorService é responsável por gerenciar as operações
// relacionadas aos administradores do sistema,
// como listar todos os administradores, buscar um administrador pelo ID,
// incluir um novo administrador, editar um administrador
// existente e deletar um administrador pelo ID.
public class AdministradorService {

    private final AdministradorRepository administradorRepository;
    // Construtor para injetar o AdministradorRepository
    public AdministradorService(AdministradorRepository administradorRepository) {
        this.administradorRepository = administradorRepository;
    }
    // Listar todos os administradores
    public List<Administrador> listarTodos() {
        return administradorRepository.findAll();
    }
    // Buscar um administrador pelo ID
    public Administrador buscarPorId(Long id) {
        Objects.requireNonNull(id, "ID do administrador não pode ser nulo");
        return administradorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Administrador não encontrado com o ID: " + id));
    }
    // Incluir um novo administrador
    public Administrador incluir(Administrador administrador) {
        Objects.requireNonNull(administrador, "Administrador não pode ser nulo");
        administrador.setId(null);
        return administradorRepository.save(administrador);
    }
    // Editar um administrador existente
    public Administrador editar(Long id, Administrador adminAtualizado) {
        Objects.requireNonNull(id, "ID do administrador não pode ser nulo");
        Optional<Administrador> existente = administradorRepository.findById(id);

        if (existente.isPresent()) {
            Administrador admin = existente.get();
            // Campos herdados de Usuario
            admin.setNome(adminAtualizado.getNome());
            admin.setEmail(adminAtualizado.getEmail());
            admin.setSenha(adminAtualizado.getSenha());
            admin.setAtivo(adminAtualizado.getAtivo());
            admin.setPerfil(adminAtualizado.getPerfil());

            // Campo específico de Administrador
            admin.setNivelAcesso(adminAtualizado.getNivelAcesso());

            return administradorRepository.save(admin);
        }

        return null;
    }
    // Deletar um administrador pelo ID
    public void deletar(Long id) {
        Objects.requireNonNull(id, "ID do administrador não pode ser nulo");
        if (administradorRepository.existsById(id)) {
            administradorRepository.deleteById(id);
        }
    }
}