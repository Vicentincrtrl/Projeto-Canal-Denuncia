package com.example.projetofatec.entities;
// Imports
public class LoginResponse {
    private Long id;
    private String nome;
    private String email;
    private String perfil;
    private Boolean ativo;

    public LoginResponse(Long id, String nome, String email, PerfilUsuario perfil, Boolean ativo) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.perfil = perfil != null ? perfil.name() : "ANALISTA";
        this.ativo = ativo;
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getEmail() { return email; }
    public String getPerfil() { return perfil; }
    public Boolean getAtivo() { return ativo; }
}