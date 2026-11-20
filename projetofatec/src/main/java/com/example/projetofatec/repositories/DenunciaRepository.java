package com.example.projetofatec.repositories;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import com.example.projetofatec.entities.Denuncia;

public interface DenunciaRepository extends JpaRepository<Denuncia, Long> {

    // Busca denúncia exata pelo número do protocolo
    Optional<Denuncia> findByProtocolo(String protocolo);

    // Busca denúncias do analista com suporte a ordenação (ex: por dataAbertura)
    List<Denuncia> findByAnalistasResponsaveisId(Long analistaId, Sort sort);

    // Busca denúncias do analista filtradas por uma categoria específica e ordenadas
    List<Denuncia> findByAnalistasResponsaveisIdAndCategoriaId(Long analistaId, Long categoriaId, Sort sort);

    // Chama a Function do SQL para gerar o protocolo
    @Query(value = "SELECT dbo.fn_gerar_protocolo()", nativeQuery = true)
    String gerarProtocoloSQL();

    // Chama a Stored Procedure do SQL Server para atribuir o analista
    @Modifying
    @Transactional
    @Query(value = "EXEC sp_atribuir_denuncia_analista :denunciaId, :analistaId", nativeQuery = true)
    void atribuirAnalistaProcedure(@Param("denunciaId") Long denunciaId, @Param("analistaId") Long analistaId);
}