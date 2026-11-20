package com.example.projetofatec.repositories;

import com.example.projetofatec.entities.Administrador;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdministradorRepository extends JpaRepository<Administrador, Long> {

    // O JpaRepository já nos dá save(), findById(), findAll(), delete(), etc.
    
}