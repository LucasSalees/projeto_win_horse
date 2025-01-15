package com.projeto.sistema.controle;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.projeto.sistema.modelos.Usuario;
import com.projeto.sistema.repositorios.UsuarioRepositorio;

@Controller
public class UsuarioControle {
	
	@Autowired
    private UsuarioRepositorio usuarioRepositorio;

	// Listar todos os garanhões
    @GetMapping("/administrativo/usuarios/listar")
    public ModelAndView listarUsuario() {
        ModelAndView mv = new ModelAndView("administrativo/usuarios/lista");
        mv.addObject("listaUsuarios", usuarioRepositorio.findAll());
        return mv;
    }
    
    // Exibir o formulário de cadastro
    @GetMapping("/administrativo/usuarios/cadastro")
    public ModelAndView cadastrar(Usuario usuario) {
        ModelAndView mv = new ModelAndView("/administrativo/usuarios/cadastro");
        mv.addObject("Usuario", usuario);
        return mv;
    }
    
    @PostMapping("/administrativo/usuarios/salvar")
    public ModelAndView salvar(@ModelAttribute Usuario usuario, BindingResult result) {
        // Validação do formulário
        if (result.hasErrors()) {
            // Se houver erros de validação, volta para o formulário com os erros
            return cadastrar(usuario);  // Retorna para o formulário com os erros
        }

        // Definir a data de cadastro (utiliza a data e hora atuais)
        usuario.setData_cadastro(LocalDateTime.now());

        // Salvar o usuário no banco de dados
        usuarioRepositorio.save(usuario);

        // Após salvar com sucesso, redireciona para a listagem dos usuários
        return new ModelAndView("redirect:/administrativo/usuarios/listar");  // Redireciona para a listagem após salvar
    }
    
 // Editar um garanhão específico pelo ID
    @GetMapping("/administrativo/usuarios/eventoUsuario/editarUsuario/{id_usuario}")
    public String editar(@PathVariable("id_usuario") Long id_usuario, Model model) {
        Optional<Usuario> Usuario = usuarioRepositorio.findById(id_usuario);
        
     // Se a movimentação for encontrada, exibe a página de edição
        if (Usuario.isPresent()) {
            model.addAttribute("usuario", Usuario.get()); // Adiciona o objeto movimentacao ao modelo
            model.addAttribute("nome_usuario", Usuario.get().getNome_usuario()); // Adiciona o nome do garanhão ao modelo
            return "administrativo/usuarios/eventoUsuario"; // Retorna para a página de edição (evento.html dentro de administrativo)
        }
     // Caso não encontre a movimentação, redireciona para a lista de movimentações
        return "redirect:/administrativo/usuarios/listar";
    }
    
    @GetMapping("/removerUsuario/{id_usuario}")
    public ModelAndView remover(@PathVariable("id_usuario") Long id_usuario) {
        usuarioRepositorio.deleteById(id_usuario); // Corrige o argumento para o método deleteById
        return listarUsuario(); // Após remover, exibe a lista de usuários
    }
    
    @PostMapping("/administrativo/usuarios/editarUsuario")
    public String salvarEdicaoUsuario(@ModelAttribute("usuario") Usuario usuario, RedirectAttributes redirectAttributes) {
        // Buscar o usuário existente pelo ID
        Optional<Usuario> usuarioExistenteOpt = usuarioRepositorio.findById(usuario.getId_usuario());
        
        if (usuarioExistenteOpt.isPresent()) {
            Usuario usuarioExistente = usuarioExistenteOpt.get();

            // Atualizar os campos editáveis do usuário
            usuarioExistente.setNome_usuario(usuario.getNome_usuario());
            usuarioExistente.setEmail(usuario.getEmail());
            usuarioExistente.setSenha(usuario.getSenha());
            usuarioExistente.setTipo(usuario.getTipo());
            usuarioExistente.setData_cadastro(usuario.getData_cadastro());

            // Salvar as atualizações no banco de dados
            usuarioRepositorio.save(usuarioExistente);

            // Mensagem de sucesso
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Usuário atualizado com sucesso.");
            return "redirect:/administrativo/usuarios/listar"; // Redireciona para a lista de usuários
        } else {
            // Caso o usuário não seja encontrado
            redirectAttributes.addFlashAttribute("mensagemErro", "Usuário não encontrado.");
            return "redirect:/administrativo/usuarios/listar";
        }
    }

}

