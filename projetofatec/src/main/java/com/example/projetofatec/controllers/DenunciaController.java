package com.example.projetofatec.controllers;

//Imports
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.example.projetofatec.entities.Denuncia;
import com.example.projetofatec.services.DenunciaService;

// Controlador REST para gerenciamento de denúncias, incluindo criação, atualização, exclusão e atribuição de analistas
@RestController
@RequestMapping("/denuncias")
@CrossOrigin(origins = "*") // Libera o acesso para o Front-end (evita bloqueio de CORS)
public class DenunciaController {

    // Classe interna para representar a requisição de parecer técnico do analista
    public static class ParecerRequest {
        private String justificativa;
        private Long statusSugerido;
        // Getters e Setters
        public String getJustificativa() {
            return justificativa;
        }
        // Getters e Setters
        public void setJustificativa(String justificativa) {
            this.justificativa = justificativa;
        }
        // Getters e Setters
        public Long getStatusSugerido() {
            return statusSugerido;
        }
        // Getters e Setters
        public void setStatusSugerido(Long statusSugerido) {
            this.statusSugerido = statusSugerido;
        }
    }
    // Injeção de dependência do Service via construtor (padrão recomendado)
    private final DenunciaService denunciaService;

    // Injeção de dependência do Service via construtor (padrão recomendado)
    public DenunciaController(DenunciaService denunciaService) {
        this.denunciaService = denunciaService;
    }

    // 1. ENDPOINT GET: Listar todas as denúncias cadastradas no sistema
    @GetMapping
    public ResponseEntity<List<Denuncia>> listar() {
        List<Denuncia> denuncias = denunciaService.listarTodas();
        return ResponseEntity.ok(denuncias);
    }

    // 2. ENDPOINT GET por ID: Buscar uma denúncia específica utilizando sua chave
    // primária
    @GetMapping("/{id}")
    public ResponseEntity<Denuncia> buscarPorId(@PathVariable Long id) {
        Denuncia denuncia = denunciaService.buscarPorId(id);
        if (denuncia != null) {
            return ResponseEntity.ok(denuncia);
        }
        return ResponseEntity.notFound().build();
    }

    // 2.1. ENDPOINT GET por Protocolo: Usado na página pública de acompanhamento
    // pelo cidadão
    @GetMapping("/protocolo/{protocolo}")
    public ResponseEntity<Denuncia> buscarPorProtocolo(@PathVariable String protocolo) {
        Denuncia denuncia = denunciaService.buscarPorProtocolo(protocolo);
        if (denuncia != null) {
            return ResponseEntity.ok(denuncia);
        }
        return ResponseEntity.notFound().build();
    }

    // 3. ENDPOINT POST: Criar e salvar uma nova denúncia enviada pelo formulário
    // público
    @PostMapping
    public ResponseEntity<Denuncia> criar(@RequestBody Denuncia denuncia) {
        Denuncia novaDenuncia = denunciaService.salvar(denuncia);
        return ResponseEntity.status(HttpStatus.CREATED).body(novaDenuncia);
    }

    // 4. ENDPOINT PUT: Atualizar os dados de uma denúncia existente
    @PutMapping("/{id}")
    public ResponseEntity<Denuncia> editar(@PathVariable Long id, @RequestBody Denuncia denuncia) {
        Denuncia atualizada = denunciaService.editar(id, denuncia);
        if (atualizada != null) {
            return ResponseEntity.ok(atualizada);
        }
        return ResponseEntity.notFound().build();
    }

    // 5. ENDPOINT DELETE: Excluir uma denúncia do banco de dados pelo ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        denunciaService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    // 6. ENDPOINT PUT (Relação N para N): Atribuir múltiplos analistas à denúncia
    // via lista de IDs no corpo
    @PutMapping("/{id}/atribuir")
    public ResponseEntity<Denuncia> atribuirAnalistas(
            @PathVariable Long id,
            @RequestBody List<Long> analistasIds) {

        Denuncia atualizada = denunciaService.atribuirAnalistas(id, analistasIds);
        return ResponseEntity.ok(atualizada);
    }

    // 7. ENDPOINT GET com Filtros: Buscar denúncias de um analista específico
    // filtrando por categoria e ordenando por data
    @GetMapping("/analista/{analistaId}")
    public ResponseEntity<List<Denuncia>> listarPorAnalista(
            @PathVariable Long analistaId,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(defaultValue = "desc") String ordem) {

        List<Denuncia> denuncias = denunciaService.buscarPorAnalistaComFiltros(analistaId, categoriaId, ordem);
        return ResponseEntity.ok(denuncias);
    }

    // 8. ENDPOINT PUT: Registrar o parecer técnico e concluir/arquivar a denúncia
    // pelo painel do analista
    @PutMapping("/{id}/parecer")
    public ResponseEntity<Denuncia> enviarParecerAnalista(
            @PathVariable Long id,
            @RequestBody ParecerRequest request) {

        Denuncia atualizada = denunciaService.enviarParecerAnalista(
                id,
                request.getJustificativa(),
                request.getStatusSugerido());
        return ResponseEntity.ok(atualizada);
    }

    // 9. ENDPOINT PUT (Rota do Admin): Julgar ou alterar o status final da denúncia
    @PutMapping("/{id}/julgar/{statusFinalId}")
    public ResponseEntity<Denuncia> julgarDenunciaAdmin(
            @PathVariable Long id,
            @PathVariable Long statusFinalId) {

        Denuncia atualizada = denunciaService.julgarDenunciaAdmin(id, statusFinalId);
        return ResponseEntity.ok(atualizada);
    }
}