package com.example.projetofatec.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/* 
 * A anotação @Entity define que esta classe é uma entidade JPA 
 * mapeada para uma tabela no banco de dados relacional (SQL Server).
 */
@Data // Lombok: Gera automaticamente getters, setters, toString, equals e hashCode
@AllArgsConstructor // Lombok: Gera construtor com todos os argumentos
@NoArgsConstructor // Lombok: Gera construtor vazio (obrigatório para o JPA/Hibernate)
@Entity
@Table(name = "denuncias") // Nome exato da tabela no banco de dados
public class Denuncia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Chave primária autoincrementada (IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String protocolo; // Código único de rastreio da denúncia

    // NVARCHAR(MAX) garante suporte a textos longos e acentuação correta no SQL Server
    @Column(nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String descricao;

    @Column(name = "data_abertura", nullable = false)
    private LocalDateTime dataAbertura; // Data e hora em que a denúncia foi registrada

    // 1. Relacionamento Muitos-para-Um (Muitas denúncias pertencem a uma categoria)
    @ManyToOne
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    // 2. Relacionamento Muitos-para-Um (Muitas denúncias possuem um status atual)
    @ManyToOne
    @JoinColumn(name = "status_id", nullable = false)
    private Status status;

    // 3. Relacionamento com Denunciante (Opcional, pois a denúncia pode ser anônima)
    @ManyToOne
    @JoinColumn(name = "denunciante_id", nullable = true)
    private Denunciante denunciante;

    // 4. Relacionamento N para N (Muitas denúncias para vários analistas)
    // O @ManyToMany mapeia a tabela associativa "analista_denuncia" no banco.
    // O @JsonIgnore foi removido daqui para que o JSON retorne os analistas responsáveis 
    // e o front-end consiga exibi-los corretamente na interface do painel!
    @ManyToMany
    @JoinTable(
        name = "analista_denuncia", 
        joinColumns = @JoinColumn(name = "denuncia_id"), 
        inverseJoinColumns = @JoinColumn(name = "analista_id")
    )
    private List<Analista> analistasResponsaveis;

    // Parecer técnico preenchido pelo analista ao concluir ou arquivar o caso
    @Column(name = "parecer_tecnico", columnDefinition = "TEXT")
    private String parecerTecnico;

    /* 
     * O gancho @PrePersist é executado automaticamente pelo JPA 
     * imediatamente antes de realizar o comando INSERT na base de dados.
     */
    @PrePersist
    public void prePersist() {
        if (this.dataAbertura == null) {
            this.dataAbertura = LocalDateTime.now(); // Atribui a data/hora atual se estiver vazia
        }
        if (this.protocolo == null || this.protocolo.isEmpty()) {
            this.protocolo = "DEN-" + System.currentTimeMillis(); // Fallback de protocolo caso a procedure não atue
        }
    }

    // Construtor customizado para facilitar a instanciação rápida via código
    public Denuncia(String descricao, Categoria categoria, Status status, Denunciante denunciante) {
        this.descricao = descricao;
        this.categoria = categoria;
        this.status = status;
        this.denunciante = denunciante;
    }

    // Métodos manuais para garantia de acesso ao parecer técnico
    public String getParecerTecnico() {
        return parecerTecnico;
    }

    public void setParecerTecnico(String parecerTecnico) {
        this.parecerTecnico = parecerTecnico;
    }
}