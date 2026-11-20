const API = "http://localhost:8080";
const corpoTabelaAdmin = document.getElementById("corpo-tabela-admin");
const corpoTabelaAnalistas = document.getElementById("corpo-tabela-analistas");
const erro = document.getElementById("mensagem-erro");

let cacheDenunciasAdmin = [];
let cacheAnalistasAdmin = [];
let chartAnalistasInstance = null;
let chartCategoriasInstance = null;

async function carregarListaAnalistas() {
  try {
    const resposta = await fetch(API + "/analistas");
    if (!resposta.ok) throw new Error("Erro ao buscar analistas");
    const analistas = await resposta.json();
    cacheAnalistasAdmin = analistas;
    
    if (corpoTabelaAnalistas) {
      corpoTabelaAnalistas.innerHTML = "";
      if (analistas.length === 0) {
        corpoTabelaAnalistas.innerHTML = `<tr><td colspan="5" style="text-align: center; padding: 10px;">Nenhum analista cadastrado.</td></tr>`;
      } else {
        analistas.forEach(a => {
          const linha = document.createElement("tr");
          linha.innerHTML = `
            <td>${a.id}</td>
            <td>${a.matricula || "N/A"}</td>
            <td>${a.nome}</td>
            <td>${a.email}</td>
            <td>
              <button onclick="excluirAnalista(${a.id})" style="background: #d9534f; color: white; border: none; padding: 5px 10px; cursor: pointer; border-radius: 4px; font-weight: bold;">Excluir</button>
            </td>
          `;
          corpoTabelaAnalistas.appendChild(linha);
        });
      }
    }

    return analistas;
  } catch (e) {
    console.error("Erro ao carregar analistas:", e);
    return [];
  }
}

window.excluirAnalista = async function(id) {
  if (!confirm("Tem certeza que deseja excluir este analista?")) return;

  try {
    const resposta = await fetch(`${API}/analistas/${id}`, { method: "DELETE" });
    if (!resposta.ok) throw new Error("Erro ao excluir analista");
    alert("Analista excluído com sucesso!");
    carregarTodasDenuncias(); 
  } catch (e) {
    console.error(e);
    alert("Não foi possível excluir o analista. Verifique se ele possui denúncias atreladas.");
  }
};

window.toggleDropdown = function(denunciaId) {
  document.querySelectorAll('[id^="dropdown-"]').forEach(el => {
    if (el.id !== `dropdown-${denunciaId}`) el.style.display = "none";
  });

  const dropdown = document.getElementById(`dropdown-${denunciaId}`);
  if (dropdown) {
    dropdown.style.display = dropdown.style.display === "block" ? "none" : "block";
  }
};

async function carregarTodasDenuncias() {
  try {
    const categoriaId = document.getElementById("filtro-categoria").value;
    const ordem = document.getElementById("filtro-ordem").value;

    const [respostaDenuncias, listaAnalistas] = await Promise.all([
      fetch(API + "/denuncias"),
      carregarListaAnalistas()
    ]);

    if (!respostaDenuncias.ok) throw new Error("Erro ao carregar denúncias");
    let denuncias = await respostaDenuncias.json();
    cacheDenunciasAdmin = denuncias;

    // --- CÁLCULO DOS KPIS E DADOS DOS GRÁFICOS ---
    let total = denuncias.length;
    let emAnalise = 0;
    let finalizadas = 0;
    let contagemCategorias = {};
    let contagemAnalistas = {};

    listaAnalistas.forEach(a => { contagemAnalistas[a.nome] = 0; });

    denuncias.forEach(d => {
      let sId = d.status ? (typeof d.status === "object" ? d.status.id : d.status) : 1;
      if (sId === 3 || sId === 4) {
        finalizadas++;
      } else {
        emAnalise++;
      }

      let catNome = d.categoria ? (d.categoria.tipoCategoria || d.categoria.nome || "Geral") : "Geral";
      contagemCategorias[catNome] = (contagemCategorias[catNome] || 0) + 1;

      if ((sId === 3 || sId === 4) && d.analistasResponsaveis) {
        d.analistasResponsaveis.forEach(an => {
          contagemAnalistas[an.nome] = (contagemAnalistas[an.nome] || 0) + 1;
        });
      }
    });

    document.getElementById("dash-total").textContent = total;
    document.getElementById("dash-analise").textContent = emAnalise;
    document.getElementById("dash-finalizadas").textContent = finalizadas;

    // --- RENDERIZAR GRÁFICO DE BARRAS (Analistas) ---
    const ctxAnalistas = document.getElementById('graficoAnalistas').getContext('2d');
    if (chartAnalistasInstance) chartAnalistasInstance.destroy();

    chartAnalistasInstance = new Chart(ctxAnalistas, {
      type: 'bar',
      data: {
        labels: Object.keys(contagemAnalistas),
        datasets: [{
          label: 'Casos Concluídos',
          data: Object.values(contagemAnalistas),
          backgroundColor: '#137333',
          borderRadius: 4
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: { legend: { display: false } },
        scales: { y: { beginAtZero: true, ticks: { stepSize: 1 } } }
      }
    });

    // --- RENDERIZAR GRÁFICO DE ROSCA (Categorias) ---
    const ctxCategorias = document.getElementById('graficoCategorias').getContext('2d');
    if (chartCategoriasInstance) chartCategoriasInstance.destroy();

    chartCategoriasInstance = new Chart(ctxCategorias, {
      type: 'doughnut',
      data: {
        labels: Object.keys(contagemCategorias),
        datasets: [{
          data: Object.values(contagemCategorias),
          backgroundColor: ['#a91b1b', '#1a73e8', '#f2994a', '#137333', '#8e44ad']
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: { 
          legend: { 
            position: 'right', 
            labels: { boxWidth: 12, font: { size: 11 } } 
          } 
        }
      }
    });
    // ---------------------------------------------

    if (categoriaId) {
      denuncias = denuncias.filter(d => d.categoria && d.categoria.id == categoriaId);
    }

    denuncias.sort((a, b) => {
      const dataA = new Date(a.dataAbertura || 0);
      const dataB = new Date(b.dataAbertura || 0);
      return ordem === "asc" ? dataA - dataB : dataB - dataA;
    });

    corpoTabelaAdmin.innerHTML = "";
    
    if (denuncias.length === 0) {
      corpoTabelaAdmin.innerHTML = `<tr><td colspan="7" style="text-align: center; padding: 20px;">Nenhuma denúncia encontrada.</td></tr>`;
      return;
    }

    denuncias.forEach(denuncia => {
      const linha = document.createElement("tr");

      let statusId = 1;
      if (denuncia.status) {
        statusId = typeof denuncia.status === "object" ? denuncia.status.id : denuncia.status;
      }

      const statusNome = denuncia.status ? (denuncia.status.nome || (statusId === 3 ? "Concluída" : statusId === 4 ? "Arquivada" : "Em Análise")) : "Pendente";
      const statusColuna = `<span style="font-weight: bold; color: ${statusId === 3 ? 'green' : statusId === 4 ? 'orange' : '#333'};">${statusNome}</span>`;

      const temAnalistas = denuncia.analistasResponsaveis && denuncia.analistasResponsaveis.length > 0;

      let atribuirColuna = "";
      if (statusId === 3 || statusId === 4) {
        atribuirColuna = `<span style="color: #666; font-style: italic;">Bloqueado (Finalizado)</span>`;
      } else if (temAnalistas) {
        atribuirColuna = `
          <div style="display: flex; gap: 8px; align-items: center;">
            <span style="background: #e6f4ea; color: #137333; padding: 4px 8px; border-radius: 4px; font-weight: bold; font-size: 12px; border: 1px solid #ceead6;">Atribuído</span>
            <button onclick="removerAtribuicao(${denuncia.id})" style="background: #d9534f; color: white; border: none; padding: 4px 8px; cursor: pointer; border-radius: 4px; font-size: 12px; font-weight: bold;" title="Remover analistas atribuídos">Cancelar</button>
          </div>
        `;
      } else {
        let checkboxesAnalistas = listaAnalistas.map(a => `
          <label style="display: block; font-size: 13px; margin-bottom: 6px; cursor: pointer; text-align: left;">
            <input type="checkbox" name="chk-analista-${denuncia.id}" value="${a.id}" style="margin-right: 6px;">
            ${a.nome}
          </label>
        `).join("");

        if (checkboxesAnalistas === "") checkboxesAnalistas = `<p style="font-size: 12px; color: #666;">Nenhum analista</p>`;

        atribuirColuna = `
          <div style="position: relative; display: inline-block;">
            <button onclick="toggleDropdown(${denuncia.id})" style="background: #f8f9fa; color: #333; border: 1px solid #ccc; padding: 6px 12px; border-radius: 4px; cursor: pointer; font-size: 13px; font-weight: bold;">
              Selecionar Analistas ▾
            </button>
            <div id="dropdown-${denuncia.id}" style="display: none; position: absolute; left: 0; top: 100%; background: white; border: 1px solid #ccc; box-shadow: 0 4px 12px rgba(0,0,0,0.15); padding: 12px; z-index: 1000; border-radius: 6px; min-width: 220px; margin-top: 4px;">
              <div style="max-height: 150px; overflow-y: auto; margin-bottom: 10px; border-bottom: 1px solid #eee; padding-bottom: 6px;">
                ${checkboxesAnalistas}
              </div>
              <button onclick="atribuir(${denuncia.id})" style="background: #a91b1b; color: white; border: none; padding: 6px 12px; cursor: pointer; border-radius: 4px; font-weight: bold; width: 100%; font-size: 13px;">Atribuir</button>
            </div>
          </div>
        `;
      }

      let nomesResponsaveis = "Nenhum";
      if (temAnalistas) {
        nomesResponsaveis = denuncia.analistasResponsaveis.map(a => `${a.nome}`).join(", ");
      }

      let dataFormatada = "Data não informada";
      const dataBruta = denuncia.dataAbertura || denuncia.dataCriacao;
      if (dataBruta) {
        if (Array.isArray(dataBruta)) {
          const [ano, mes, dia, hora = 0, min = 0] = dataBruta;
          const dataObj = new Date(ano, mes - 1, dia, hora, min);
          dataFormatada = dataObj.toLocaleDateString("pt-BR");
        } else {
          const dataObj = new Date(dataBruta);
          if (!isNaN(dataObj)) dataFormatada = dataObj.toLocaleDateString("pt-BR");
        }
      }

      linha.innerHTML = `
        <td>${denuncia.protocolo}</td>
        <td>${denuncia.categoria ? denuncia.categoria.tipoCategoria : "Geral"}</td>
        <td>
          <button onclick="abrirModalDescricao(${denuncia.id})" style="background: #2b7a78; color: white; border: none; padding: 5px 10px; border-radius: 4px; cursor: pointer; font-size: 12px; font-weight: bold;">Ver Denúncia</button>
        </td>
        <td>${dataFormatada}</td>
        <td>${statusColuna}</td>
        <td class="td-atribuicao">${atribuirColuna}</td>
        <td>${nomesResponsaveis}</td>
      `;
      corpoTabelaAdmin.appendChild(linha);
    });
  } catch (e) {
    erro.textContent = "Erro ao buscar denúncias para o administrador.";
    console.error(e);
  }
}

// --- FUNÇÃO PARA GERAR O RELATÓRIO EXECUTIVO FORMATADO COM GRÁFICOS ---
window.gerarRelatorioExecutivo = function() {
  const janelaRelatorio = window.open('', '_blank');
  
  let total = document.getElementById("dash-total").textContent;
  let analise = document.getElementById("dash-analise").textContent;
  let finalizadas = document.getElementById("dash-finalizadas").textContent;
  let dataEmissao = new Date().toLocaleDateString("pt-BR") + " às " + new Date().toLocaleTimeString("pt-BR");

  let imgAnalistas = chartAnalistasInstance ? chartAnalistasInstance.toBase64Image() : '';
  let imgCategorias = chartCategoriasInstance ? chartCategoriasInstance.toBase64Image() : '';

  let linhasTabelaRelatorio = cacheDenunciasAdmin.map(d => {
    let statusNome = d.status ? (d.status.nome || (d.status.id === 3 ? "Concluída" : d.status.id === 4 ? "Arquivada" : "Em Análise")) : "Em Análise";
    let analistasStr = d.analistasResponsaveis && d.analistasResponsaveis.length > 0 ? d.analistasResponsaveis.map(a => a.nome).join(", ") : "Não atribuído";
    let parecerStr = d.parecerTecnico ? d.parecerTecnico : "Nenhum parecer registrado.";
    let dataF = "N/A";
    if (d.dataAbertura) {
      if (Array.isArray(d.dataAbertura)) {
        const [ano, mes, dia] = d.dataAbertura;
        dataF = `${dia}/${mes}/${ano}`;
      } else {
        dataF = new Date(d.dataAbertura).toLocaleDateString("pt-BR");
      }
    }

    return `
      <tr>
        <td style="padding: 8px; border: 1px solid #ddd; font-weight: bold;">${d.protocolo}</td>
        <td style="padding: 8px; border: 1px solid #ddd;">${d.categoria ? d.categoria.tipoCategoria : "Geral"}</td>
        <td style="padding: 8px; border: 1px solid #ddd;">${dataF}</td>
        <td style="padding: 8px; border: 1px solid #ddd;">${statusNome}</td>
        <td style="padding: 8px; border: 1px solid #ddd;">${analistasStr}</td>
        <td style="padding: 8px; border: 1px solid #ddd; font-size: 11px; color: #333;">${parecerStr}</td>
      </tr>
    `;
  }).join("");

  janelaRelatorio.document.write(`
    <!DOCTYPE html>
    <html lang="pt-BR">
    <head>
      <meta charset="UTF-8">
      <title>Relatório Executivo - Canal de Denúncias TARRAF</title>
      <style>
        body { font-family: Arial, sans-serif; color: #333; margin: 30px; }
        .cabecalho-relatorio { border-bottom: 3px solid #a91b1b; padding-bottom: 15px; margin-bottom: 25px; display: flex; justify-content: space-between; align-items: center; }
        .marca-relatorio { font-size: 24px; font-weight: bold; color: #a91b1b; }
        .marca-relatorio span { color: #333; font-size: 16px; display: block; font-weight: normal; }
        .info-meta { font-size: 13px; color: #666; text-align: right; }
        .resumo-kpis { display: flex; gap: 15px; margin-bottom: 25px; }
        .kpi-card { background: #f9f9f9; border: 1px solid #ddd; padding: 15px; border-radius: 6px; flex: 1; text-align: center; }
        .kpi-card h4 { margin: 0 0 5px 0; font-size: 12px; color: #555; text-transform: uppercase; }
        .kpi-card p { margin: 0; font-size: 20px; font-weight: bold; color: #a91b1b; }
        .secao-graficos { display: flex; justify-content: space-around; gap: 20px; margin: 25px 0; page-break-inside: avoid; }
        .bloco-grafico { border: 1px solid #ddd; padding: 15px; border-radius: 6px; text-align: center; flex: 1; background: #fff; }
        .bloco-grafico h4 { margin: 0 0 10px 0; font-size: 13px; color: #444; border-bottom: 1px solid #eee; padding-bottom: 5px; }
        .bloco-grafico img { max-height: 160px; width: auto; display: block; margin: 0 auto; }
        table { width: 100%; border-collapse: collapse; margin-top: 15px; font-size: 12px; }
        th { background: #a91b1b; color: white; padding: 10px; border: 1px solid #a91b1b; text-align: left; }
        .rodape { margin-top: 40px; text-align: center; font-size: 11px; color: #777; border-top: 1px solid #ddd; padding-top: 10px; }
        @media print {
          button { display: none; }
        }
      </style>
    </head>
    <body>
      <div class="cabecalho-relatorio">
        <div class="marca-relatorio">
          TARRAF <span>Canal Corporativo de Denúncias e Compliance</span>
        </div>
        <div class="info-meta">
          <strong>Relatório Executivo Consolidado</strong><br>
          Emitido em: ${dataEmissao}
        </div>
      </div>

      <p style="font-size: 14px; line-height: 1.5;">
        Abaixo apresenta-se o relatório executivo consolidado contendo o panorama geral das ocorrências registradas no sistema, a distribuição gráfica analítica de desempenho da equipe e das categorias de denúncia, bem como o consolidado analítico com todos os pareceres técnicos emitidos.
      </p>

      <div class="resumo-kpis">
        <div class="kpi-card">
          <h4>Total de Denúncias</h4>
          <p>${total}</p>
        </div>
        <div class="kpi-card">
          <h4>Em Análise</h4>
          <p style="color: #1a73e8;">${analise}</p>
        </div>
        <div class="kpi-card">
          <h4>Concluídas / Arquivadas</h4>
          <p style="color: #137333;">${finalizadas}</p>
        </div>
      </div>

      <h3 style="color: #a91b1b; border-bottom: 2px solid #eee; padding-bottom: 5px; margin-top: 25px;">Indicadores Gráficos Analíticos</h3>
      <div class="secao-graficos">
        <div class="bloco-grafico">
          <h4>Desempenho de Analistas (Entregas)</h4>
          <img src="${imgAnalistas}" alt="Gráfico de Desempenho" />
        </div>
        <div class="bloco-grafico">
          <h4>Distribuição por Categoria</h4>
          <img src="${imgCategorias}" alt="Gráfico de Categorias" />
        </div>
      </div>

      <h3 style="color: #a91b1b; border-bottom: 2px solid #eee; padding-bottom: 5px; margin-top: 30px;">Consolidado de Ocorrências e Pareceres Técnicos</h3>
      <table>
        <thead>
          <tr>
            <th>Protocolo</th>
            <th>Categoria</th>
            <th>Data</th>
            <th>Status</th>
            <th>Analista(s)</th>
            <th>Parecer Técnico Registrado</th>
          </tr>
        </thead>
        <tbody>
          ${linhasTabelaRelatorio}
        </tbody>
      </table>

      <div style="margin-top: 30px; text-align: right;">
        <button onclick="window.print()" style="background: #a91b1b; color: white; border: none; padding: 10px 20px; font-weight: bold; border-radius: 4px; cursor: pointer; font-size: 14px;">🖨️ Imprimir / Salvar PDF</button>
      </div>

      <div class="rodape">
        Documento gerado automaticamente pelo Sistema Corporativo de Ouvidoria e Denúncias TARRAF. Uso restrito e confidencial.
      </div>
    </body>
    </html>
  `);
  janelaRelatorio.document.close();
};

window.abrirModalDescricao = function(denunciaId) {
  const denuncia = cacheDenunciasAdmin.find(d => d.id === denunciaId);
  if (!denuncia) return;

  document.getElementById("modal-titulo").textContent = `Protocolo: ${denuncia.protocolo}`;
  document.getElementById("modal-texto-descricao").textContent = denuncia.descricao || "Nenhuma descrição informada.";

  const containerParecer = document.getElementById("modal-container-parecer");
  const textoParecer = document.getElementById("modal-texto-parecer");

  if (denuncia.parecerTecnico) {
    containerParecer.style.display = "block";
    textoParecer.textContent = denuncia.parecerTecnico;
  } else {
    containerParecer.style.display = "none";
  }

  document.getElementById("modal-descricao").style.display = "flex";
};

window.fecharModalDescricao = function() {
  document.getElementById("modal-descricao").style.display = "none";
};

document.getElementById("form-cadastrar-analista").addEventListener("submit", async function (e) {
  e.preventDefault();

  const matricula = document.getElementById("novo-matricula").value.trim();
  const nome = document.getElementById("novo-nome").value.trim();
  const email = document.getElementById("novo-email").value.trim();
  const senha = document.getElementById("novo-senha").value.trim();
  const msgCadastro = document.getElementById("mensagem-cadastro");

  try {
    const resposta = await fetch(API + "/analistas", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ matricula, nome, email, senha, perfil: "ANALISTA", ativo: true })
    });

    if (!resposta.ok) throw new Error("Erro ao cadastrar analista");

    msgCadastro.style.color = "green";
    msgCadastro.textContent = "Analista cadastrado com sucesso!";
    document.getElementById("form-cadastrar-analista").reset();
    carregarTodasDenuncias(); 
  } catch (err) {
    console.error(err);
    msgCadastro.style.color = "red";
    msgCadastro.textContent = "Erro ao cadastrar analista.";
  }
});

document.getElementById("btn-filtrar").addEventListener("click", carregarTodasDenuncias);

window.atribuir = async function(denunciaId) {
  const checkboxes = document.querySelectorAll(`input[name="chk-analista-${denunciaId}"]:checked`);
  const analistasIds = Array.from(checkboxes).map(cb => Number(cb.value));

  if (analistasIds.length === 0) {
    alert("Selecione pelo menos um analista na caixinha.");
    return;
  }

  try {
    const resposta = await fetch(`${API}/denuncias/${denunciaId}/atribuir`, {
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(analistasIds)
    });

    if (!resposta.ok) throw new Error("Erro ao atribuir analistas");
    alert("Analistas atribuídos com sucesso!");
    carregarTodasDenuncias();
  } catch (e) {
    alert("Erro ao realizar atribuição múltipla.");
    console.error(e);
  }
};

window.removerAtribuicao = async function(denunciaId) {
  if (!confirm("Tem certeza que deseja cancelar a atribuição desta denúncia?")) return;

  try {
    const resposta = await fetch(`${API}/denuncias/${denunciaId}/atribuir`, {
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify([])
    });

    if (!resposta.ok) throw new Error("Erro ao remover atribuição");
    alert("Atribuição cancelada com sucesso!");
    carregarTodasDenuncias();
  } catch (e) {
    alert("Erro ao cancelar atribuição.");
    console.error(e);
  }
};

document.getElementById("botao-sair").addEventListener("click", function () {
  sessionStorage.removeItem("logado");
  localStorage.removeItem("usuarioLogado");
  window.location.href = "login.html";
});

carregarTodasDenuncias();