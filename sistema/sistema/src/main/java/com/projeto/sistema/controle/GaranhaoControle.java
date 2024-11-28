package com.projeto.sistema.controle;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.projeto.sistema.modelos.Garanhao;

import com.projeto.sistema.repositorios.GaranhaoRepositorio;

@Controller
public class GaranhaoControle {

    @Autowired
    private GaranhaoRepositorio garanhaoRepositorio;

    // Exibir o formulário de cadastro
    @GetMapping("/administrativo/garanhoes/cadastro")
    public ModelAndView cadastrar(Garanhao garanhao) {
        ModelAndView mv = new ModelAndView("/administrativo/garanhoes/cadastro");
        mv.addObject("garanhao", garanhao);
        return mv;
    }

    // Listar todos os garanhões
    @GetMapping("/administrativo/garanhoes/listar")
    public ModelAndView listarGaranhoes() {
        ModelAndView mv = new ModelAndView("administrativo/garanhoes/lista");
        mv.addObject("listaGaranhoes", garanhaoRepositorio.findAll());
        return mv;
    }

    // Editar um garanhão específico pelo ID
    @GetMapping("/administrativo/garanhoes/eventoGaranhao/editarGaranhao/{id_garanhao}")
    public String editar(@PathVariable("id_garanhao") Long id_garanhao, Model model) {
        Optional<Garanhao> Garanhao = garanhaoRepositorio.findById(id_garanhao);
        
     // Se a movimentação for encontrada, exibe a página de edição
        if (Garanhao.isPresent()) {
            model.addAttribute("garanhao", Garanhao.get()); // Adiciona o objeto movimentacao ao modelo
            model.addAttribute("nome_garanhao", Garanhao.get().getNome_garanhao()); // Adiciona o nome do garanhão ao modelo
            return "administrativo/garanhoes/eventoGaranhao"; // Retorna para a página de edição (evento.html dentro de administrativo)
        }
     // Caso não encontre a movimentação, redireciona para a lista de movimentações
        return "redirect:/administrativo/garanhoes/listar";
    }
    
    @PostMapping("/administrativo/garanhoes/eventoGaranhao/editarGaranhao/{id_garanhao}")
    public String salvarEdicao(@PathVariable("id_garanhao") Long id_garanhao, @ModelAttribute Garanhao garanhaoAtualizado) {
        // Busca o garanhão existente no banco de dados
        Optional<Garanhao> garanhaoExistente = garanhaoRepositorio.findById(id_garanhao);

        if (garanhaoExistente.isPresent()) {
            Garanhao garanhao = garanhaoExistente.get();
            
            // Atualiza apenas os campos que podem ser alterados
            garanhao.setNome_garanhao(garanhaoAtualizado.getNome_garanhao());
            garanhao.setCor_palheta(garanhaoAtualizado.getCor_palheta());
            garanhao.setBotijao(garanhaoAtualizado.getBotijao());
            garanhao.setSaldo_inicial_palhetas(garanhaoAtualizado.getSaldo_inicial_palhetas());
            garanhao.setCaneca(garanhaoAtualizado.getCaneca());
            garanhao.setData_contagem_inicial(garanhaoAtualizado.getData_contagem_inicial());
            garanhao.setData_cadastro(garanhaoAtualizado.getData_cadastro());
            garanhao.setSaldo_atual_palhetas(garanhaoAtualizado.getSaldo_atual_palhetas());
            

            // Salva as alterações no banco de dados
            garanhaoRepositorio.save(garanhao);
        }

        // Redireciona para a lista de garanhões após a edição
        return "redirect:/administrativo/garanhoes/listar";
    }


    

    @PostMapping("/administrativo/garanhoes/salvar")
    public ModelAndView salvar(@ModelAttribute Garanhao garanhao, BindingResult result) {
        // Validação do formulário
        if (result.hasErrors()) {
            // Se houver erros de validação, volta para o formulário com os erros
            return cadastrar(garanhao);  // Retorna para o formulário com os erros
        }

        // Definir a data de cadastro (utiliza a data e hora atuais)
        garanhao.setData_cadastro(LocalDateTime.now());

        // Verificar o saldo inicial de palhetas e definir o saldo atual
        if (garanhao.getSaldo_inicial_palhetas() > 0) {
            garanhao.setSaldo_atual_palhetas(garanhao.getSaldo_inicial_palhetas());
        } else {
            // Caso o saldo inicial seja 0 ou negativo, o saldo atual também será 0
            garanhao.setSaldo_atual_palhetas(0);
        }

        try {
            // Salvando o Garanhão
            garanhaoRepositorio.save(garanhao);  // Salva sem precisar de flush, o Spring cuida disso
        } catch (Exception e) {
            // Caso ocorra algum erro ao salvar, você pode capturar e tratar aqui
            e.printStackTrace();  // Exibe o erro detalhado no log para diagnóstico
            result.rejectValue("nome_garanhao", "error.garanhao", "Erro ao salvar o Garanhão. Tente novamente.");
            return cadastrar(garanhao);  // Retorna para o formulário com a mensagem de erro
        }

        // Após salvar com sucesso, redireciona para a listagem dos Garanhões
        return new ModelAndView("redirect:/administrativo/garanhoes/listar");  // Redireciona para a listagem após salvar
    }

    // Remover um garanhão pelo ID
    @GetMapping("/administrativo/garanhoes/remover/{id_garanhao}")
    public ModelAndView remover(@PathVariable("id_garanhao") Long id_garanhao) {
        try {
            garanhaoRepositorio.deleteById(id_garanhao);
        } catch (Exception e) {
            e.printStackTrace();
            // Adicione um mecanismo de mensagem de erro aqui, se necessário
        }
        return listarGaranhoes();
    }

    @PostMapping("/administrativo/garanhoes/ajustarSaldo")
    public ModelAndView ajustarSaldo(Long idGaranhao, int quantidade) {
        if (quantidade <= 0) {
            // Caso a quantidade seja inválida
            return listarGaranhoes();
        }

        Optional<Garanhao> garanhaoOpt = garanhaoRepositorio.findById(idGaranhao);

        if (garanhaoOpt.isPresent()) {
            Garanhao garanhao = garanhaoOpt.get();
            int novoSaldo = garanhao.getSaldo_atual_palhetas() - quantidade;

            if (novoSaldo < 0) {
                // Se o saldo for insuficiente, retorne uma mensagem de erro
                return listarGaranhoes();
            }

            // Atualiza o saldo e salva o Garanhao
            garanhao.setSaldo_atual_palhetas(novoSaldo);
            garanhaoRepositorio.save(garanhao);
        } else {
            // Se o Garanhão não for encontrado, retorne um erro ou mensagem
        }

        return listarGaranhoes();
    }

    @GetMapping("/administrativo/garanhoes/dados/{id}")
    @ResponseBody
    public Map<String, Object> obterDadosGaranhao(@PathVariable("id") Long id) {
        Map<String, Object> response = new HashMap<>();

        try {
            Optional<Garanhao> garanhaoOpt = garanhaoRepositorio.findById(id);
            if (garanhaoOpt.isPresent()) {
                Garanhao garanhao = garanhaoOpt.get();

                // Adicionando os dados ao mapa de resposta
                response.put("caneca", garanhao.getCaneca());
                response.put("saldo_atual_palhetas", garanhao.getSaldo_atual_palhetas());
                response.put("botijao", garanhao.getBotijao());
                response.put("cor_palheta", garanhao.getCor_palheta());
                response.put("quantidade", garanhao.getQuantidade());
                response.put("data_contagem_inicial", garanhao.getData_contagem_inicial());
                response.put("data_cadastro", garanhao.getData_cadastro());
            } else {
                response.put("error", "Garanhão não encontrado.");
            }
        } catch (Exception e) {
            response.put("error", "Erro ao buscar dados do garanhão.");
            e.printStackTrace();
        }

        return response;
    }
}
