package com.example.projetofatec.controllers;
//Imports
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.projetofatec.entities.LoginResponse;
import com.example.projetofatec.entities.PerfilUsuario;

// Controlador REST para autenticação de usuários (login)
@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    // Injeção do EntityManager para consultas diretas ao banco de dados
    @PersistenceContext
    private EntityManager entityManager;
    // Classe interna para representar a requisição de login
    public static class LoginRequest {
        private String email;
        private String senha;
        // Getters e Setters
        public String getEmail() {
            return email;
        }
        // Getters e Setters
        public void setEmail(String email) {
            this.email = email;
        }
        //  Getters e Setters
        public String getSenha() {
            return senha;
        }
        //  Getters e Setters
        public void setSenha(String senha) {
            this.senha = senha;
        }
    }
    // Classe interna para representar a resposta de login
    // Endpoint POST para autenticação de usuários
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            if (request.getEmail() == null || request.getSenha() == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("E-mail ou senha não informados.");
            }

            String emailLimpo = request.getEmail().trim();
            String senhaLimpa = request.getSenha().trim();

            // Consulta direta na tabela de usuários trazendo apenas os campos primitivos
            Object[] resultado;
            try {
                resultado = (Object[]) entityManager.createQuery(
                        "SELECT u.id, u.nome, u.email, u.senha, u.perfil, u.ativo FROM Usuario u WHERE LOWER(u.email) = LOWER(:email)")
                        .setParameter("email", emailLimpo)
                        .getSingleResult();
            } catch (NoResultException e) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("E-mail ou senha incorretos.");
            }
            // Mapeia os resultados da consulta para variáveis locais
            Long id = (Long) resultado[0];
            String nome = (String) resultado[1];
            String emailDb = (String) resultado[2];
            String senhaDb = (String) resultado[3];
            PerfilUsuario perfilEnum = (PerfilUsuario) resultado[4];
            Boolean ativo = (Boolean) resultado[5];
            // Valida a senha
            if (senhaDb != null && senhaDb.trim().equals(senhaLimpa)) {
                LoginResponse response = new LoginResponse(id, nome, emailDb, perfilEnum, ativo);
                return ResponseEntity.ok(response);
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("E-mail ou senha incorretos.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro interno no servidor.");
        }
    }
}