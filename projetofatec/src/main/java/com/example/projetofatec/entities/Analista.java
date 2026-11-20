package com.example.projetofatec.entities;
// Imports
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.PrimaryKeyJoinColumn;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "analistas")
@PrimaryKeyJoinColumn(name = "usuario_id")
// A classe Analista representa um usuário com privilégios de analista no sistema, estendendo a classe base Usuario.
public class Analista extends Usuario {

    @Column(nullable = false, unique = true, length = 50)
    private String matricula;

    public Analista(String nome, String email, String senha, String matricula) {
        super(nome, email, senha, PerfilUsuario.ANALISTA);
        this.matricula = matricula;
    }
}