# Canal de Denúncias — TARRAF

Sistema web corporativo para registro, triagem, análise técnica, parecer, conclusão/arquivamento e auditoria de denúncias, desenvolvido como projeto acadêmico com **Java (Spring Boot)**, **HTML/CSS/JavaScript** e **Microsoft SQL Server**.

---

## Visão Geral e Arquitetura

A aplicação segue uma arquitetura em camadas integrando interface web, API/serviços em Spring Boot e base relacional no SQL Server:

* **Front-end (HTML, CSS e JavaScript):**
  * Página de abertura de denúncias com seleção de categoria e geração/exibição de protocolo.
  * Tela de login e controle de sessão com diferenciação de acesso por perfil.
  * Painel do **Administrador** (visualização de casos, indicadores/KPIs, atribuição de analistas e deliberação/julgamento com conclusão ou arquivamento).
  * Painel do **Analista** (consulta de denúncias atribuídas e registro de parecer técnico).
* **Back-end (Java / Spring Boot):**
  * Entidades mapeadas no pacote `entities` com anotações de persistência e relacionamentos (incluindo herança/especialização de usuários e associação N:N entre denúncias e analistas).
  * Camadas de acesso a dados e regras de aplicação conectadas ao SQL Server, realizando operações CRUD, consultas consolidadas, chamadas a procedures e validações de permissão.
* **Banco de Dados (SQL Server):**
  * Base `projetofatec` com tabelas de domínio (`categorias`, `status`, `denunciantes`), usuários e perfis (`usuarios`, `administradores`, `analistas`), tabela principal (`denuncias`), tabela associativa N:N (`analista_denuncia`) e trilha de auditoria (`historico_denuncias`), além de índices em colunas de busca e status.

---

## Funcionalidades e Regras de Negócio

1. **Registro de Denúncias:** O denunciante informa a categoria e a descrição do relato; o sistema gera um protocolo único e registra a denúncia com status inicial **Pendente**.
2. **Autenticação e Níveis de Acesso:** Usuários autenticam-se no sistema e são direcionados conforme o perfil (`ADMINISTRADOR` ou `ANALISTA`), restringindo ações administrativas (como atribuir analistas ou concluir/arquivar) a quem possui permissão.
3. **Atribuição a Analistas (Relacionamento N:N):** O administrador vincula uma ou mais denúncias a analistas via tabela intermediária `analista_denuncia`, movendo o caso para **Em Análise**.
4. **Parecer Técnico e Julgamento:** O analista registra o parecer técnico da apuração e o administrador finaliza o fluxo marcando a denúncia como **Concluída** ou **Arquivada**.
5. **Auditoria e Compliance no Banco:**
   * Alterações de status em `denuncias` disparam trigger de auditoria registrando status anterior, novo status, observação, autor e data/hora em `historico_denuncias`.
   * Regras de compliance no banco bloqueiam exclusão direta de denúncias e alterações/exclusões indevidas nas categorias fixas.

---

## Recursos Avançados no SQL Server

O script do banco contempla:

* **Views:**
  * `vw_painel_analista`: consolida dados da denúncia, categoria, status e analista atribuído (nome e matrícula).
  * `vw_relatorio_denuncias`: visão executiva com protocolo, categoria, data de abertura, status atual e parecer técnico (com fallback quando pendente).
  * `vw_kpis_dashboard`: agrega a quantidade de denúncias por status para métricas do painel.
  * `vw_historico_detalhado`: exibe a trilha de auditoria com nomes legíveis dos status anterior e novo.
* **Functions:**
  * `fn_gerar_protocolo`: função escalar para geração padronizada de código de protocolo.
  * `fn_calcular_tempo_resolucao`: função escalar que calcula os dias decorridos desde a abertura da denúncia.
  * `fn_buscar_denuncias_por_analista`: função de tabela (*inline*) que retorna as denúncias vinculadas a um analista específico.
* **Stored Procedures:**
  * `sp_atribuir_denuncia_analista`: associa analista à denúncia na relação N:N e atualiza o status para **Em Análise** dentro de transação.
  * `sp_concluir_denuncia`: registra o parecer técnico e atualiza o status para **Concluída** de forma transacional.
  * `sp_arquivar_denuncia`: atualiza o status da denúncia para **Arquivada** de forma transacional.
* **Triggers:**
  * `trg_impedir_delete_denuncia`: bloqueia `DELETE` direto na tabela `denuncias` por regra de compliance.
  * `trg_log_alteracao_status`: registra automaticamente o histórico na tabela `historico_denuncias` quando o `status_id` é alterado.
  * `trg_bloquear_alteracao_categoria`: impede `UPDATE` e `DELETE` na tabela `categorias`.

---

## Estrutura Sugerida do Repositório

canal-denuncias-tarraf/
├── backend/                 # Projeto Spring Boot (src, pom.xml / build.gradle, resources)
├── frontend/                # Arquivos HTML, CSS e JS (login, painéis e abertura de denúncia)
├── database/                # Script .sql completo (DDL, Views, Functions, SPs, Triggers e Carga)
├── .gitignore
└── README.md
