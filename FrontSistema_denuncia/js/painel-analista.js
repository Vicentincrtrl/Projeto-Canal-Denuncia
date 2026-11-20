const API = "http://localhost:8080";
const corpoTabelaAnalista = document.getElementById("corpo-tabela-analista");
const erro = document.getElementById("mensagem-erro");
const spanSaudacao = document.getElementById("saudacao-analista");

// Elementos dos KPIs
const kpiTotal = document.getElementById("kpi-total");
const kpiAnalise = document.getElementById("kpi-analise");
const kpiFinalizadas = document.getElementById("kpi-finalizadas");

const usuarioLogado = JSON.parse(localStorage.getItem("usuarioLogado") || sessionStorage.getItem("usuarioLogado"));

if (!usuarioLogado || usuarioLogado.perfil !== "ANALISTA") {
  window.location.href = "login.html";
} else {
  spanSaudacao.textContent = `Olá, ${usuarioLogado.nome}`;
}

let cacheDenunciasAnalista = [];

async function carregarDenunciasAnalista() {
  try {
    const categoriaId = document.getElementById("filtro-categoria")?.value || "";
    const ordem = document.getElementById("filtro-ordem")?.value || "desc";

    let url = `${API}/denuncias/analista/${usuarioLogado.id}`;
    const params = new URLSearchParams();
    if (categoriaId) params.append("categoriaId", categoriaId);
    if (ordem) params.append("ordem", ordem);
    if (params.toString()) url += `?${params.toString()}`;

    const resposta = await fetch(url);
    if (!resposta.ok) throw new Error("Erro ao buscar suas denúncias");
    
    const denuncias = await resposta.json();
    cacheDenunciasAnalista = denuncias;

    // Cálculo dos KPIs
    let total = denuncias.length;
    let emAnalise = 0;
    let finalizadas = 0;

    denuncias.forEach(d => {
      let sId = d.status ? (typeof d.status === "object" ? d.status.id : d.status) : 1;
      if (sId === 3 || sId === 4) {
        finalizadas++;
      } else {
        emAnalise++;
      }
    });

    if (kpiTotal) kpiTotal.textContent = total;
    if (kpiAnalise) kpiAnalise.textContent = emAnalise;
    if (kpiFinalizadas) kpiFinalizadas.textContent = finalizadas;

    corpoTabelaAnalista.innerHTML = "";

    if (denuncias.length === 0) {
      corpoTabelaAnalista.innerHTML = `<tr><td colspan="5" style="text-align: center; padding: 20px;">Nenhuma denúncia atribuída a você no momento.</td></tr>`;
      return;
    }

    denuncias.forEach(denuncia => {
      const linha = document.createElement("tr");

      let statusId = denuncia.status ? (typeof denuncia.status === "object" ? denuncia.status.id : denuncia.status) : 1;
      
      let statusColuna = "";
      if (statusId === 3) {
        statusColuna = `<span style="background: #e6f4ea; color: #137333; padding: 4px 10px; border-radius: 4px; font-weight: bold; font-size: 12px; border: 1px solid #ceead6;">✔ Concluída</span>`;
      } else if (statusId === 4) {
        statusColuna = `<span style="background: #fef7e0; color: #b06000; padding: 4px 10px; border-radius: 4px; font-weight: bold; font-size: 12px; border: 1px solid #fef1c7;">📁 Arquivada</span>`;
      } else {
        statusColuna = `<span style="background: #e8f0fe; color: #1a73e8; padding: 4px 10px; border-radius: 4px; font-weight: bold; font-size: 12px; border: 1px solid #d2e3fc;">⏳ Em Análise</span>`;
      }

      let dataFormatada = "Data não informada";
      const dataBruta = denuncia.dataAbertura || denuncia.dataCriacao;
      if (dataBruta) {
        if (Array.isArray(dataBruta)) {
          const [ano, mes, dia, hora = 0, min = 0] = dataBruta;
          dataFormatada = new Date(ano, mes - 1, dia, hora, min).toLocaleDateString("pt-BR");
        } else {
          const dataObj = new Date(dataBruta);
          if (!isNaN(dataObj)) dataFormatada = dataObj.toLocaleDateString("pt-BR");
        }
      }

      linha.innerHTML = `
        <td>${denuncia.protocolo}</td>
        <td>${denuncia.categoria ? denuncia.categoria.tipoCategoria : "Geral"}</td>
        <td>${dataFormatada}</td>
        <td>${statusColuna}</td>
        <td>
          <button onclick="abrirModalAvaliacao(${denuncia.id})" style="background: #a91b1b; color: white; border: none; padding: 6px 12px; border-radius: 4px; cursor: pointer; font-size: 12px; font-weight: bold;">Ver / Avaliar</button>
        </td>
      `;
      corpoTabelaAnalista.appendChild(linha);
    });

  } catch (e) {
    erro.textContent = "Erro ao carregar painel do analista.";
    console.error(e);
  }
}

// Injeta a Modal no HTML dinamicamente com suporte para exibir o parecer técnico anterior
if (!document.getElementById("modal-avaliacao")) {
  const modalDiv = document.createElement("div");
  modalDiv.id = "modal-avaliacao";
  modalDiv.style.cssText = "display: none; position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.5); z-index: 2000; justify-content: center; align-items: center;";
  modalDiv.innerHTML = `
    <div style="background: white; padding: 25px; border-radius: 8px; width: 90%; max-width: 600px; box-shadow: 0 4px 20px rgba(0,0,0,0.2); position: relative; max-height: 90vh; overflow-y: auto;">
      <h3 id="modal-titulo" style="margin-top: 0; color: #a91b1b; border-bottom: 2px solid #eee; padding-bottom: 10px;">Avaliar Denúncia</h3>
      
      <p style="margin: 15px 0 5px 0; font-weight: bold; color: #555;">Descrição Completa:</p>
      <div id="modal-descricao-texto" style="background: #f9f9f9; padding: 12px; border: 1px solid #ddd; border-radius: 4px; max-height: 120px; overflow-y: auto; font-size: 14px; color: #333; margin-bottom: 15px; white-space: pre-wrap;"></div>

      <!-- Bloco para exibir o Parecer Técnico caso já esteja finalizada -->
      <div id="modal-container-parecer-salvo" style="display: none; margin-bottom: 15px;">
        <p style="margin: 15px 0 5px 0; font-weight: bold; color: #137333;">Parecer Técnico Registrado:</p>
        <div id="modal-texto-parecer-salvo" style="background: #e6f4ea; padding: 12px; border: 1px solid #ceead6; border-radius: 4px; max-height: 120px; overflow-y: auto; white-space: pre-wrap; font-size: 14px; color: #137333;"></div>
      </div>

      <div id="modal-container-parecer">
        <label style="font-weight: bold; display: block; margin-bottom: 5px;">Parecer Técnico / Justificativa:</label>
        <textarea id="modal-txt-justificativa" placeholder="Digite o parecer técnico detalhado..." style="width: 100%; height: 80px; padding: 8px; border: 1px solid #ccc; border-radius: 4px; margin-bottom: 15px; resize: vertical;"></textarea>
        
        <label style="font-weight: bold; display: block; margin-bottom: 5px;">Ação / Status Final:</label>
        <select id="modal-select-status" style="width: 100%; padding: 8px; border: 1px solid #ccc; border-radius: 4px; margin-bottom: 20px;">
          <option value="3">Concluir Denúncia</option>
          <option value="4">Arquivar Denúncia</option>
        </select>
      </div>

      <div style="text-align: right; display: flex; justify-content: flex-end; gap: 10px;">
        <button onclick="fecharModalAvaliacao()" style="background: #333; color: white; border: none; padding: 8px 16px; border-radius: 4px; cursor: pointer; font-weight: bold;">Fechar</button>
        <button id="modal-btn-enviar" onclick="enviarParecerModal()" style="background: #a91b1b; color: white; border: none; padding: 8px 16px; border-radius: 4px; cursor: pointer; font-weight: bold;">Salvar Parecer</button>
      </div>
    </div>
  `;
  document.body.appendChild(modalDiv);
}

let denunciaAtualId = null;

window.abrirModalAvaliacao = function(id) {
  denunciaAtualId = id;
  const denuncia = cacheDenunciasAnalista.find(d => d.id === id);
  if (!denuncia) return;

  document.getElementById("modal-titulo").textContent = `Protocolo: ${denuncia.protocolo}`;
  document.getElementById("modal-descricao-texto").textContent = denuncia.descricao || "Sem descrição.";

  let statusId = denuncia.status ? (typeof denuncia.status === "object" ? denuncia.status.id : denuncia.status) : 1;
  const containerParecer = document.getElementById("modal-container-parecer");
  const containerParecerSalvo = document.getElementById("modal-container-parecer-salvo");
  const textoParecerSalvo = document.getElementById("modal-texto-parecer-salvo");
  const btnEnviar = document.getElementById("modal-btn-enviar");

  if (statusId === 3 || statusId === 4) {
    containerParecer.style.display = "none";
    btnEnviar.style.display = "none";
    
    // Se houver parecer técnico salvo, exibe a caixinha verde
    if (denuncia.parecerTecnico) {
      containerParecerSalvo.style.display = "block";
      textoParecerSalvo.textContent = denuncia.parecerTecnico;
    } else {
      containerParecerSalvo.style.display = "none";
    }
  } else {
    containerParecer.style.display = "block";
    containerParecerSalvo.style.display = "none";
    btnEnviar.style.display = "inline-block";
    document.getElementById("modal-txt-justificativa").value = "";
  }

  document.getElementById("modal-avaliacao").style.display = "flex";
};

window.fecharModalAvaliacao = function() {
  document.getElementById("modal-avaliacao").style.display = "none";
};

window.enviarParecerModal = async function() {
  const justificativa = document.getElementById("modal-txt-justificativa").value.trim();
  const statusSugerido = Number(document.getElementById("modal-select-status").value);

  if (!justificativa) {
    alert("Por favor, preencha a justificativa do parecer.");
    return;
  }

  try {
    const resposta = await fetch(`${API}/denuncias/${denunciaAtualId}/parecer`, {
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ justificativa, statusSugerido })
    });

    if (!resposta.ok) throw new Error("Erro ao enviar parecer");

    alert("Parecer enviado e denúncia encerrada com sucesso!");
    fecharModalAvaliacao();
    carregarDenunciasAnalista();
  } catch (e) {
    alert("Erro ao processar o envio do parecer.");
    console.error(e);
  }
};

document.getElementById("btn-filtrar")?.addEventListener("click", carregarDenunciasAnalista);

document.getElementById("botao-sair").addEventListener("click", function () {
  sessionStorage.removeItem("logado");
  localStorage.removeItem("usuarioLogado");
  window.location.href = "login.html";
});

carregarDenunciasAnalista();