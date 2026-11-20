package com.example.projetofatec.services;
// Imports
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Service;
import com.example.projetofatec.entities.Analista;
import com.example.projetofatec.repositories.AnalistaRepository;

@Service
// A classe AnalistaService é responsável por gerenciar as operações
public class AnalistaService {

    private final AnalistaRepository analistaRepository;
    // Construtor para injetar o AnalistaRepository
    public AnalistaService(AnalistaRepository analistaRepository) {
        this.analistaRepository = analistaRepository;
    }
    // Listar todos os analistas
    public List<Analista> listarTodos() {
        return analistaRepository.findAll();
    }
    // Buscar um analista pelo ID
    public Analista buscarPorId(Long id) {
        Objects.requireNonNull(id, "ID do analista não pode ser nulo");
        return analistaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Analista não encontrado com o ID: " + id));
    }
    // Incluir um novo analista
    public Analista incluir(Analista analista) {
        Objects.requireNonNull(analista, "Analista não pode ser nulo");
        analista.setId(null);
        return analistaRepository.save(analista);
    }
    // Editar um analista existente
    public Analista editar(Long id, Analista analistaAtualizado) {
        Objects.requireNonNull(id, "ID do analista não pode ser nulo");
        Optional<Analista> existente = analistaRepository.findById(id);

        if (existente.isPresent()) {
            Analista analista = existente.get();
            // Campos herdados de Usuario
            analista.setNome(analistaAtualizado.getNome());
            analista.setEmail(analistaAtualizado.getEmail());
            analista.setSenha(analistaAtualizado.getSenha());
            analista.setAtivo(analistaAtualizado.getAtivo());
            analista.setPerfil(analistaAtualizado.getPerfil());
            
            // Campo específico de Analista
            analista.setMatricula(analistaAtualizado.getMatricula());

            return analistaRepository.save(analista);
        }

        return null;
    }
    // Deletar um analista pelo ID
    public void deletar(Long id) {
        Objects.requireNonNull(id, "ID do analista não pode ser nulo");
        if (analistaRepository.existsById(id)) {
            analistaRepository.deleteById(id);
        }
    }
}