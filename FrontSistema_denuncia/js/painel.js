const API = "http://localhost:8080";

// TEMPORÁRIO: analista fixo, até existir o login real no back-end
const ANALISTA_ID = 4;

// Se não fez login, volta para a tela de login
if (sessionStorage.getItem("logado") !== "sim") {
  window.location.href = "login.html";
}

// Ids conforme o banco. Depois podemos buscar em GET /status.
const listaStatus = [
  { id: 1, nome: "Pendente" },
  { id: 2, nome: "Em Análise" },
  { id: 3, nome: "Concluída" },
  { id: 4, nome: "Arquivada" }
];

const corpoTabela = document.getElementById("corpo-tabela");
const detalhe = document.getElementById("detalhe");
const erro = document.getElementById("mensagem-erro");

function criarCelula(texto) {
  const celula = document.createElement("td");
  celula.textContent = texto;
  return celula;
}

function mostrarDetalhe(denuncia) {
  document.getElementById("det-protocolo").textContent = denuncia.protocolo;
  document.getElementById("det-descricao").textContent = denuncia.descricao;
  detalhe.hidden = false;
}

function criarSeletorStatus(denuncia) {
  const seletor = document.createElement("select");
  let statusAtual = denuncia.status.id;

  listaStatus.forEach(function (status) {
    const opcao = document.createElement("option");
    opcao.value = status.id;
    opcao.textContent = status.nome;
    seletor.appendChild(opcao);
  });
  seletor.value = statusAtual;

  seletor.addEventListener("change", async function () {
    try {
      const resposta = await fetch(
        API + "/denuncias/" + denuncia.id + "/status/" + seletor.value,
        { method: "PUT" }
      );
      if (!resposta.ok) {
        throw new Error("Erro " + resposta.status);
      }
      statusAtual = Number(seletor.value);
      erro.textContent = "";
    } catch (e) {
      seletor.value = statusAtual; // volta ao valor anterior
      erro.textContent = "Não foi possível alterar o status.";
      console.error(e);
    }
  });

  return seletor;
}

function montarTabela(denuncias) {
  corpoTabela.innerHTML = "";

  denuncias.forEach(function (denuncia) {
    const linha = document.createElement("tr");

    linha.appendChild(criarCelula(denuncia.protocolo));
    linha.appendChild(criarCelula(denuncia.categoria.tipoCategoria));
    linha.appendChild(
      criarCelula(new Date(denuncia.dataAbertura).toLocaleDateString("pt-BR"))
    );

    const celulaStatus = document.createElement("td");
    celulaStatus.appendChild(criarSeletorStatus(denuncia));
    linha.appendChild(celulaStatus);

    const celulaBotao = document.createElement("td");
    const botao = document.createElement("button");
    botao.textContent = "Ver";
    botao.addEventListener("click", function () {
      mostrarDetalhe(denuncia);
    });
    celulaBotao.appendChild(botao);
    linha.appendChild(celulaBotao);

    corpoTabela.appendChild(linha);
  });
}

async function carregarDenuncias() {
  try {
    const resposta = await fetch(API + "/denuncias/analista/" + ANALISTA_ID);
    if (!resposta.ok) {
      throw new Error("Erro " + resposta.status);
    }
    const denuncias = await resposta.json();

    if (denuncias.length === 0) {
      erro.textContent = "Nenhuma denúncia atribuída.";
      return;
    }
    montarTabela(denuncias);
  } catch (e) {
    erro.textContent = "Não foi possível carregar as denúncias.";
    console.error(e);
  }
}

document.getElementById("botao-sair").addEventListener("click", function () {
  sessionStorage.removeItem("logado");
  window.location.href = "login.html";
});

carregarDenuncias();