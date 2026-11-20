package com.example.projetofatec.services;
// Imports
import com.example.projetofatec.entities.Categoria;
import com.example.projetofatec.repositories.CategoriaRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service 
// A classe CategoriaService é responsável por gerenciar as operações relacionadas às categorias no sistema, incluindo listagem, busca, inclusão e exclusão de categorias.
public class CategoriaService {

    // Injeção do repositório de categorias para realizar operações de banco de dados
    private final CategoriaRepository categoriaRepository;

    CategoriaService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    // 1. Método para listar todas as categorias cadastradas
    public List<Categoria> listarTodas() {
        return categoriaRepository.findAll();
    }

    // 2. Método para buscar uma categoria pelo ID
    public Categoria buscarPorId( Long id) {
        Optional<Categoria> categoria = categoriaRepository.findById(id);
        return categoria.orElseThrow(() -> new RuntimeException("Categoria não encontrada com o ID: " + id));
    }

    // 3. Método para salvar uma nova categoria
    public Categoria salvar(Categoria categoria) {
        // Aqui você poderia colocar regras, ex: verificar se o nome já existe antes de salvar
        return categoriaRepository.save(categoria);
    }

    // 4. Método para deletar uma categoria
    public void deletar(Long id) {
        categoriaRepository.deleteById(id);
    }
}

