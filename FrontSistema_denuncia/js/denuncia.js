const API = "http://localhost:8080";

const form = document.getElementById("form-denuncia");
const select = document.getElementById("categoria");
const descricao = document.getElementById("descricao");
const erro = document.getElementById("mensagem-erro");
const confirmacao = document.getElementById("confirmacao");
const protocoloSpan = document.getElementById("protocolo");

// 1. Carregar as categorias reais do banco de dados ao abrir a página
async function carregarCategorias() {
  try {
    const resposta = await fetch(API + "/categorias");
    if (!resposta.ok) throw new Error("Erro ao carregar categorias");
    const categorias = await resposta.json();

    categorias.forEach(function (categoria) {
      const opcao = document.createElement("option");
      opcao.value = categoria.id;
      opcao.textContent = categoria.tipoCategoria;
      select.appendChild(opcao);
    });
  } catch (e) {
    console.error(e);
    erro.textContent = "Não foi possível carregar as categorias do servidor.";
  }
}

carregarCategorias();

// 2. Enviar a nova denúncia para o Spring Boot
form.addEventListener("submit", async function (evento) {
  evento.preventDefault();

  const texto = descricao.value.trim();
  if (texto.length < 20) {
    erro.textContent = "Descreva com pelo menos 20 caracteres.";
    return;
  }

  erro.textContent = "";

  const dadosDenuncia = {
    descricao: texto,
    categoria: {
      id: Number(select.value)
    },
    status: {
      id: 1 // Status 1 = Pendente
    },
    denunciante: {
      id: 1 // Denunciante padrão anônimo
    }
  };

  try {
    const resposta = await fetch(API + "/denuncias", {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify(dadosDenuncia)
    });

    if (!resposta.ok) throw new Error("Erro ao salvar denúncia");

    const denunciaCriada = await resposta.json();

    // Exibe o protocolo gerado automaticamente pela Function do SQL Server
    protocoloSpan.textContent = denunciaCriada.protocolo;

    form.hidden = true;
    confirmacao.hidden = false;
  } catch (e) {
    console.error(e);
    erro.textContent = "Erro ao enviar a denúncia. Verifique se a API está rodando.";
  }
});