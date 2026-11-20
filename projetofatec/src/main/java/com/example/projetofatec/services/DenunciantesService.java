package com.example.projetofatec.services;
// Imports
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;
import com.example.projetofatec.entities.Denuncia;
import com.example.projetofatec.repositories.DenuncianteRepository;

@Service
// A classe DenunciantesService é responsável por gerenciar as operações relacionadas aos denunciantes no sistema, incluindo listagem, busca e inclusão de denunciantes.
public class DenunciantesService {

    private final DenuncianteRepository denuncianteRepository;
    // Construtor para injetar o DenuncianteRepository
    public DenunciantesService(DenuncianteRepository denuncianteRepository) {
        this.denuncianteRepository = denuncianteRepository;
    }
    // Listar todos os denunciantes
    public List<Denuncia> listarTodos() {
        return denuncianteRepository.findAll();
    }
    // Buscar um denunciante pelo ID
    public Denuncia buscarPorId(Long id) {
        Objects.requireNonNull(id, "ID não pode ser nulo");
        return denuncianteRepository.findById(id).orElse(null);
    }
    // Incluir um novo denunciante
    public Denuncia incluir(Denuncia denunciante) {
        Objects.requireNonNull(denunciante, "Denunciante não pode ser nulo");
        denunciante.setId(null);
        return denuncianteRepository.save(denunciante);
    }
}