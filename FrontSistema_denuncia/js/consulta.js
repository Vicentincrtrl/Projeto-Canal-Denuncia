const API = "http://localhost:8080";

const form = document.getElementById("form-consulta");
const campo = document.getElementById("protocolo-busca");
const erro = document.getElementById("mensagem-erro");
const resultado = document.getElementById("resultado");

form.addEventListener("submit", async function (evento) {
  evento.preventDefault();

  const protocolo = campo.value.trim();
  erro.textContent = "";
  resultado.hidden = true;

  try {
    const resposta = await fetch(API + "/denuncias/protocolo/" + encodeURIComponent(protocolo));
    
    if (resposta.status === 404) {
      erro.textContent = "Protocolo não encontrado. Confira o número e tente novamente.";
      return;
    }
    
    if (!resposta.ok) throw new Error("Erro na consulta");

    const denuncia = await resposta.json();
    console.log("Denúncia recebida:", denuncia);

    // 1. Protocolo
    document.getElementById("res-protocolo").textContent = denuncia.protocolo || protocolo;

    // 2. Categoria
    let categoriaTexto = "Geral";
    if (denuncia.categoria) {
      categoriaTexto = denuncia.categoria.tipoCategoria || denuncia.categoria.nome || "Geral";
    }
    document.getElementById("res-categoria").textContent = categoriaTexto;

    // 3. Data de Abertura
    let dataFormatada = "Data não informada";
    const dataBruta = denuncia.dataAbertura || denuncia.dataCriacao;
    
    if (dataBruta) {
      if (Array.isArray(dataBruta)) {
        const [ano, mes, dia, hora = 0, min = 0] = dataBruta;
        const dataObj = new Date(ano, mes - 1, dia, hora, min);
        dataFormatada = dataObj.toLocaleDateString("pt-BR") + " às " + String(hora).padStart(2, '0') + ":" + String(min).padStart(2, '0');
      } else {
        const dataObj = new Date(dataBruta);
        if (!isNaN(dataObj.getTime())) {
          dataFormatada = dataObj.toLocaleDateString("pt-BR") + " às " + dataObj.toLocaleTimeString("pt-BR", { hour: '2-digit', minute: '2-digit' });
        } else {
          dataFormatada = String(dataBruta);
        }
      }
    }
    document.getElementById("res-data").textContent = dataFormatada;

    // 4. Status
    let statusTexto = "Pendente";
    if (denuncia.status) {
      if (typeof denuncia.status === "object") {
        statusTexto = denuncia.status.tipoStatus || denuncia.status.nome || (denuncia.status.id === 3 ? "Concluída" : denuncia.status.id === 4 ? "Arquivada" : "Em Análise");
      } else if (denuncia.status === 3) {
        statusTexto = "Concluída";
      } else if (denuncia.status === 4) {
        statusTexto = "Arquivada";
      } else if (denuncia.status === 2) {
        statusTexto = "Em Análise";
      }
    }
    document.getElementById("res-status").textContent = statusTexto;

    // 5. Exibição do Parecer Técnico (Caso exista)
    const blocoParecer = document.getElementById("bloco-parecer");
    const txtParecer = document.getElementById("res-parecer");

    if (denuncia.parecerTecnico && denuncia.parecerTecnico.trim() !== "") {
      txtParecer.textContent = denuncia.parecerTecnico;
      blocoParecer.hidden = false;
    } else {
      blocoParecer.hidden = true;
    }
    
    resultado.hidden = false;
  } catch (e) {
    console.error(e);
    erro.textContent = "Não foi possível realizar a consulta ao servidor.";
  }
});