package com.example.projetofatec.entities;
// Imports
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor 
@NoArgsConstructor 
@Entity
@Table(name = "administradores")
@PrimaryKeyJoinColumn(name = "usuario_id")
// A classe Administrador representa um usuário com privilégios administrativos no sistema, estendendo a classe base Usuario.
public class Administrador extends Usuario {

    @Column(name = "nivel_acesso", length = 100, nullable = false)
    private String nivelAcesso;

    public Administrador(String nome, String email, String senha, String nivelAcesso) {
        super(nome, email, senha, PerfilUsuario.ADMINISTRADOR);
        this.nivelAcesso = nivelAcesso;
    }
}