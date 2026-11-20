package com.example.projetofatec.services;

import com.example.projetofatec.entities.Status;
import com.example.projetofatec.repositories.StatusRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class StatusService {

    private final StatusRepository statusRepository;

    public StatusService(StatusRepository statusRepository) {
        this.statusRepository = statusRepository;
    }

    // 1. Listar todos os status
    public List<Status> listarTodos() {
        return statusRepository.findAll();
    }

    // 2. Buscar status pelo ID
    public Status buscarPorId( Long id) {
        Optional<Status> status = statusRepository.findById(id);
        return status.orElseThrow(() -> new RuntimeException("Status não encontrado com o ID: " + id));
    }

    // 3. Salvar o status
    public Status salvar(Status status) {
        return statusRepository.save(status);
    }

    // 4. Deletar o status pelo ID
    public void deletar(Long id) {
        statusRepository.deleteById(id);
    }
}

// === PONTOS A OBSERVAR NAS REGRAS DO STATUS ===
