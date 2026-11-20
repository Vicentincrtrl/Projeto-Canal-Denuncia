package com.example.projetofatec.entities;

// Imports
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.OneToMany;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "denunciantes")
// A classe Denunciante representa um denunciante anônimo no sistema, com um
// identificador único e uma lista de denúncias associadas.
public class Denunciante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relacionamento 1 para N cortado para evitar loop infinito no JSON
    @JsonIgnore
    @OneToMany(mappedBy = "denunciante")
    private List<Denuncia> denuncias;

}