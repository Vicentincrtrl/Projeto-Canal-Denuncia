package com.example.projetofatec.repositories;

import com.example.projetofatec.entities.Analista;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnalistaRepository extends JpaRepository<Analista, Long> {

    // O JpaRepository já nos dá save(), findById(), findAll(), delete(), etc.
    
}