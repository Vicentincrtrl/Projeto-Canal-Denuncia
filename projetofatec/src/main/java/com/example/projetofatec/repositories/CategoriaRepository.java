package com.example.projetofatec.repositories;

import com.example.projetofatec.entities.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    // O JpaRepository já nos dá save(), findById(), findAll(), delete(), etc.
    
}