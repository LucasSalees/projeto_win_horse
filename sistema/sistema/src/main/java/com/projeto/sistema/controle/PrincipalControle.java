package com.projeto.sistema.controle;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.projeto.sistema.modelos.Usuario;
import com.projeto.sistema.repositorios.UsuarioRepositorio;

import jakarta.servlet.http.HttpSession;

@Controller
public class PrincipalControle {

    @Autowired
    private UsuarioRepositorio usuarioRepositorio;

    // Rota para a página inicial, que redireciona para o login
    @RequestMapping("/")
    public String redirectToLogin(HttpSession session) {
        // Verifica se há um usuário logado na sessão
        if (session.getAttribute("usuarioLogado") != null) {
            return "redirect:/home";  // Se já estiver logado, redireciona para o home
        }
        return "redirect:/administrativo";  // Senão, redireciona para a página de login
    }

    // Rota para a página de login (login.jsp ou login.html)
    @GetMapping("/administrativo")
    public String acessarLogin(HttpSession session) {
        // Verifica se o usuário já está logado
        if (session.getAttribute("usuarioLogado") != null) {
            return "redirect:/home";  // Se já estiver logado, redireciona para o home
        }
        return "administrativo/login";  // Caso contrário, exibe a página de login
    }

    // Rota para o login
    @PostMapping("/login")
    public String login(Model model, Usuario usrParam, HttpSession session) {
        // Verifica o usuário no banco de dados
        Usuario usr = this.usuarioRepositorio.Login(usrParam.getEmail(), usrParam.getSenha());

        if (usr != null) {
            // Se o login for bem-sucedido, armazena o usuário na sessão
            session.setAttribute("usuarioLogado", usr);
            return "redirect:/home";  // Redireciona para a página inicial (home)
        }

        // Se o login falhar, retorna a página de login com uma mensagem de erro
        model.addAttribute("erro", "Usuário ou senha inválidos");
        return "administrativo/login";
    }

    @GetMapping("/home")
    public String home(HttpSession session, Model model) {
        // Verifica se o usuário está logado
        if (session.getAttribute("usuarioLogado") == null) {
            return "redirect:/administrativo";  // Se não estiver logado, redireciona para o login
        }
        
        // Obtém o usuário da sessão e adiciona ao modelo
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
        model.addAttribute("usuario", usuario);
        
        return "administrativo/home";  // Exibe a página principal (home)
    }

    // Rota de logout
    @GetMapping("/sair")
    public String logout(HttpSession session) {
        // Limpa a sessão para deslogar o usuário
        session.invalidate();  // Finaliza a sessão
        return "redirect:/administrativo";  // Redireciona para a página de login
    }
}
