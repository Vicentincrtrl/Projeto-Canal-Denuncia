package com.example.projetofatec.repositories;

import com.example.projetofatec.entities.Status;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StatusRepository extends JpaRepository<Status, Long> {

    // O JpaRepository já nos dá save(), findById(), findAll(), delete(), etc.
    
}