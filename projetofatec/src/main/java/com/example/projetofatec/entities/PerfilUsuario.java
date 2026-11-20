package com.example.projetofatec.entities;
// Imports
public enum PerfilUsuario {
    ANALISTA,
    ADMINISTRADOR;

    public String getDescricao() {
        switch (this) {
            case ANALISTA:
                return "Analista";

            default:
                return "Administrador";
        }
    }

}
