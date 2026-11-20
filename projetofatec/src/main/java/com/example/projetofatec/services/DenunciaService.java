package com.example.projetofatec.services;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.projetofatec.entities.Analista;
import com.example.projetofatec.entities.Denuncia;
import com.example.projetofatec.entities.Status;
import com.example.projetofatec.repositories.AnalistaRepository;
import com.example.projetofatec.repositories.DenunciaRepository;
import com.example.projetofatec.repositories.StatusRepository;

/* 
 * Camada de Serviço (Service) responsável por conter toda a regra de negócio 
 * do sistema de denúncias, intermediando os Controllers e os Repositories.
 */
@Service
public class DenunciaService {

    private final DenunciaRepository denunciaRepository;
    private final StatusRepository statusRepository;
    private final AnalistaRepository analistaRepository; // Injetado para buscar os analistas na relação N:N

    // Injeção de dependências dos Repositórios via construtor
    public DenunciaService(DenunciaRepository denunciaRepository,
            AnalistaRepository analistaRepository,
            StatusRepository statusRepository) {
        this.denunciaRepository = denunciaRepository;
        this.analistaRepository = analistaRepository;
        this.statusRepository = statusRepository;
    }

    // Retorna a lista completa de todas as denúncias cadastradas
    public List<Denuncia> listarTodas() {
        return denunciaRepository.findAll();
    }

    // Busca uma denúncia específica pelo seu ID (chave primária)
    public Denuncia buscarPorId(Long id) {
        Objects.requireNonNull(id, "ID não pode ser nulo");
        return denunciaRepository.findById(id).orElse(null);
    }

    // Salva uma nova denúncia gerando o protocolo automaticamente via função do SQL Server
    public Denuncia salvar(Denuncia denuncia) {
        Objects.requireNonNull(denuncia, "Denúncia não pode ser nula");
        denuncia.setId(null); // Garante que será um comando INSERT no banco

        // DELEGAÇÃO: Pede para o SQL Server gerar a string do protocolo via Function customizada
        String protocoloGerado = denunciaRepository.gerarProtocoloSQL();
        denuncia.setProtocolo(protocoloGerado);

        return denunciaRepository.save(denuncia);
    }

    // Atualiza os dados de uma denúncia existente
    public Denuncia editar(Long id, Denuncia denunciaAtualizada) {
        Objects.requireNonNull(id, "ID não pode ser nulo");
        Optional<Denuncia> existente = denunciaRepository.findById(id);

        if (existente.isPresent()) {
            Denuncia d = existente.get();
            d.setDescricao(denunciaAtualizada.getDescricao());
            d.setCategoria(denunciaAtualizada.getCategoria());
            d.setStatus(denunciaAtualizada.getStatus());
            d.setAnalistasResponsaveis(denunciaAtualizada.getAnalistasResponsaveis());

            return denunciaRepository.save(d);
        }
        return null;
    }

    // Exclui uma denúncia do banco de dados pelo ID
    public void deletar(Long id) {
        Objects.requireNonNull(id, "ID não pode ser nulo");
        if (denunciaRepository.existsById(id)) {
            denunciaRepository.deleteById(id);
        }
    }

    // ATRIBUIÇÃO SIMPLES (Mantida para compatibilidade, se necessário): Aciona Stored Procedure do banco
    @Transactional
    public Denuncia atribuirAnalista(Long denunciaId, Long analistaId) {
        Objects.requireNonNull(denunciaId, "ID da denúncia não pode ser nulo");
        Objects.requireNonNull(analistaId, "ID do analista não pode ser nulo");

        // DELEGAÇÃO: Manda o SQL Server rodar a Procedure (insere N:N e altera status)
        denunciaRepository.atribuirAnalistaProcedure(denunciaId, analistaId);

        return denunciaRepository.findById(denunciaId)
                .orElseThrow(() -> new RuntimeException("Denúncia não encontrada após atribuição"));
    }

    // ATRIBUIÇÃO MÚLTIPLA (Relação N para N): Associa vários analistas de uma vez só via lista de IDs
    @Transactional
    public Denuncia atribuirAnalistas(Long denunciaId, List<Long> analistasIds) {
        Objects.requireNonNull(denunciaId, "ID da denúncia não pode ser nulo");
        Objects.requireNonNull(analistasIds, "A lista de IDs de analistas não pode ser nula");

        // Busca a denúncia alvo ou dispara exceção se não existir
        Denuncia denuncia = buscarPorId(denunciaId);
        if (denuncia == null) {
            throw new RuntimeException("Denúncia não encontrada com ID: " + denunciaId);
        }

        // Busca no banco todos os analistas cujos IDs foram selecionados na interface
        List<Analista> analistas = analistaRepository.findAllById(analistasIds);

        // Atualiza a relação N para N (tabela associativa) com os novos analistas escolhidos
        denuncia.setAnalistasResponsaveis(analistas);

        // Salva e persiste a alteração na base de dados
        return denunciaRepository.save(denuncia);
    }

    // Busca denúncias associadas a um analista específico
    public List<Denuncia> buscarPorAnalista(Long analistaId) {
        Objects.requireNonNull(analistaId, "ID do analista não pode ser nulo");
        return denunciaRepository.findByAnalistasResponsaveisId(analistaId, Sort.unsorted());
    }

    // Busca denúncias de um analista com opções de ordenação por data e filtro opcional por categoria
    public List<Denuncia> buscarPorAnalistaComFiltros(Long analistaId, Long categoriaId, String ordemData) {
        Objects.requireNonNull(analistaId, "ID do analista não pode ser nulo");

        Sort.Direction direction = "asc".equalsIgnoreCase(ordemData) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, "dataAbertura");

        if (categoriaId != null) {
            return denunciaRepository.findByAnalistasResponsaveisIdAndCategoriaId(analistaId, categoriaId, sort);
        }

        return denunciaRepository.findByAnalistasResponsaveisId(analistaId, sort);
    }

    // AÇÃO DO ANALISTA: Registra o parecer técnico e define o status sugerido (3 - Concluída ou 4 - Arquivada)
    public Denuncia enviarParecerAnalista(Long denunciaId, String justificativa, Long statusSugeridoId) {
        if (statusSugeridoId != 3L && statusSugeridoId != 4L) {
            throw new RuntimeException("O status sugerido deve ser 3 (Concluída) ou 4 (Arquivada).");
        }
        Denuncia denuncia = buscarPorId(denunciaId);
        String textoSugestao = (statusSugeridoId == 3L) ? "CONCLUIR DENÚNCIA" : "ARQUIVAR DENÚNCIA";
        String parecerFormatado = "Parecer do Analista (" + textoSugestao + "):\n" + justificativa;

        denuncia.setParecerTecnico(parecerFormatado);

        Status statusNovo = statusRepository.findById(statusSugeridoId)
                .orElseThrow(() -> new RuntimeException("Status não encontrado"));
        denuncia.setStatus(statusNovo);

        return denunciaRepository.save(denuncia);
    }

    // AÇÃO DO ADMINISTRADOR: Valida o parecer e emite a decisão final (3 - Concluída ou 4 - Arquivada)
    public Denuncia julgarDenunciaAdmin(Long denunciaId, Long statusFinalId) {
        if (statusFinalId != 3L && statusFinalId != 4L) {
            throw new RuntimeException("Operação inválida. O status final deve ser 3 (Concluída) ou 4 (Arquivada).");
        }

        Denuncia denuncia = buscarPorId(denunciaId);

        Status statusFinal = statusRepository.findById(statusFinalId)
                .orElseThrow(() -> new RuntimeException("Status não encontrado"));
        denuncia.setStatus(statusFinal);

        return denunciaRepository.save(denuncia);
    }
    
    // Busca uma denúncia específica pelo número do protocolo (consulta pública do cidadão)
    public Denuncia buscarPorProtocolo(String protocolo) {
        Objects.requireNonNull(protocolo, "Protocolo não pode ser nulo");
        return denunciaRepository.findByProtocolo(protocolo).orElse(null);
    }
}