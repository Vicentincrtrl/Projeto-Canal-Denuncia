package com.example.projetofatec.entities;

// Imports
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
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
@Table(name = "status")
// A classe Status representa o status de uma denúncia no sistema, com um
// identificador único, um tipo de status e uma observação opcional.
public class Status {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tipo_status", nullable = false, length = 50)
    private String tipoStatus;

    @Column(length = 255)
    private String observacao;

    // Relacionamento 1 para N cortado para evitar loop no JSON
    @JsonIgnore
    @OneToMany(mappedBy = "status")
    private List<Denuncia> denuncias;

    public Status(String tipoStatus, String observacao) {
        this.tipoStatus = tipoStatus;
        this.observacao = observacao;
    }
}