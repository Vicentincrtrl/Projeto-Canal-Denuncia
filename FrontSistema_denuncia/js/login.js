const API = "http://localhost:8080";

const formLogin = document.getElementById("form-login");
const mensagemErro = document.getElementById("mensagem-erro");

if (formLogin) {
  formLogin.addEventListener("submit", async function (evento) {
    evento.preventDefault();

    const email = document.getElementById("email").value.trim();
    const senha = document.getElementById("senha").value.trim();
    mensagemErro.textContent = "";

    try {
      const resposta = await fetch(API + "/auth/login", {
        method: "POST",
        headers: {
          "Content-Type": "application/json"
        },
        body: JSON.stringify({ email, senha })
      });

      if (!resposta.ok) {
        throw new Error("Credenciais inválidas");
      }

      const usuarioLogado = await resposta.json();
      console.log("Login efetuado com sucesso:", usuarioLogado);

      // Salva os dados da sessão
      localStorage.setItem("usuarioLogado", JSON.stringify(usuarioLogado));
      sessionStorage.setItem("logado", "sim");

      // Verifica o perfil para mandar para a tela correta
      const perfil = usuarioLogado.perfil ? usuarioLogado.perfil.toUpperCase() : "";

      if (perfil.includes("ADMIN")) {
        window.location.href = "painel-admin.html";
      } else {
        window.location.href = "painel-analista.html";
      }

    } catch (e) {
      console.error(e);
      mensagemErro.textContent = "E-mail ou senha incorretos. Tente novamente.";
    }
  });
}