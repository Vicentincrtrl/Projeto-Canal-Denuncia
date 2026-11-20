package com.example.projetofatec.entities;

// Imports
import jakarta.persistence.Table;
import jakarta.persistence.Entity;
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
@Table(name = "categorias")
// A classe Categoria representa uma categoria de denúncia no sistema, com um identificador único e um tipo de categoria.
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; 

    @Column(name = "tipo_categoria", nullable = false, length = 100)
    private String tipoCategoria; 

    // Relacionamento 1 para N cortado para evitar loop no JSON
    @JsonIgnore
    @OneToMany(mappedBy = "categoria")
    private List<Denuncia> denuncias;

    public Categoria(String tipoCategoria) {
        this.tipoCategoria = tipoCategoria;
    }
}