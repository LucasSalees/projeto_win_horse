package com.projeto.sistema.controle;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.projeto.sistema.modelos.Garanhao;
import com.projeto.sistema.modelos.Movimentacao;
import com.projeto.sistema.repositorios.GaranhaoRepositorio;
import com.projeto.sistema.repositorios.MovimentacaoRepositorio;

@Controller
public class GaranhaoControle {

    @Autowired
    private GaranhaoRepositorio garanhaoRepositorio;
    
    @Autowired
    private MovimentacaoRepositorio movimentacaoRepositorio;

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
    
    @PostMapping("/administrativo/garanhoes/editarGaranhao")
    public String salvarEdicaoGaranhao(@ModelAttribute("garanhao") Garanhao garanhao, RedirectAttributes redirectAttributes) {
        // Buscar o garanhão existente pelo ID
        Optional<Garanhao> garanhaoExistenteOpt = garanhaoRepositorio.findById(garanhao.getId_garanhao());

        if (garanhaoExistenteOpt.isPresent()) {
            Garanhao garanhaoExistente = garanhaoExistenteOpt.get();

            // Garantir que o saldo inicial não seja negativo
            if (garanhao.getSaldo_inicial_palhetas() < 0) {
                redirectAttributes.addFlashAttribute("mensagemErro", "O saldo de palhetas não pode ser negativo.");
                return "redirect:/administrativo/garanhoes/listar";
            }

            // Atualizar apenas os campos alterados
            atualizarCamposGaranhao(garanhao, garanhaoExistente);

            // Salvar as atualizações no banco de dados
            garanhaoRepositorio.save(garanhaoExistente);

            // Mensagem de sucesso
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Garanhão atualizado com sucesso.");
            return "redirect:/administrativo/garanhoes/listar";
        } else {
            // Caso o garanhão não seja encontrado
            redirectAttributes.addFlashAttribute("mensagemErro", "Garanhão não encontrado.");
            return "redirect:/administrativo/garanhoes/listar";
        }
    }

    // Método auxiliar para atualizar os campos
    private void atualizarCamposGaranhao(Garanhao novo, Garanhao existente) {
        // Verifica e atualiza o saldo inicial e ajusta o saldo atual
        int saldoInicialAnterior = existente.getSaldo_inicial_palhetas();
        int saldoAtualAnterior = existente.getSaldo_atual_palhetas();

        existente.setSaldo_inicial_palhetas(novo.getSaldo_inicial_palhetas());
        if (saldoInicialAnterior != novo.getSaldo_inicial_palhetas()) {
            int diferenca = novo.getSaldo_inicial_palhetas() - saldoInicialAnterior;
            existente.setSaldo_atual_palhetas(saldoAtualAnterior + diferenca);
        }

        // Atualizar outros campos
        if (!Objects.equals(existente.getNome_garanhao(), novo.getNome_garanhao())) {
            existente.setNome_garanhao(novo.getNome_garanhao());
        }

        if (existente.getBotijao() != novo.getBotijao()) {
            existente.setBotijao(novo.getBotijao());
        }

        if (existente.getCaneca() != novo.getCaneca()) {
            existente.setCaneca(novo.getCaneca());
        }

        if (!Objects.equals(existente.getCor_palheta(), novo.getCor_palheta())) {
            existente.setCor_palheta(novo.getCor_palheta());
        }

        // Atualiza apenas a data de contagem inicial, se necessária
        if (novo.getData_contagem_inicial() != null) {
            existente.setData_contagem_inicial(novo.getData_contagem_inicial());
        }
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

    @GetMapping("/removerGaranhao/{id_garanhao}")
    public ResponseEntity<String> remover(@PathVariable("id_garanhao") Long id_garanhao) {
        System.out.println("ID recebido para exclusão: " + id_garanhao); // Log para verificar o ID

        try {
            // Busca movimentações associadas ao garanhão pelo ID
            List<Movimentacao> movimentacoesAssociadas = movimentacaoRepositorio.findAll()
                    .stream()
                    .filter(movimentacao -> movimentacao.getGaranhao().getId_garanhao().equals(id_garanhao))
                    .collect(Collectors.toList());

            if (!movimentacoesAssociadas.isEmpty()) {
                // Caso existam movimentações associadas
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Erro ao excluir: Garanhão possui movimentações associadas.");
            }

            // Se não houver movimentações, tenta excluir o garanhão
            garanhaoRepositorio.deleteById(id_garanhao);
            return ResponseEntity.ok("Garanhão removido com sucesso!");

        } catch (Exception e) {
            // Caso ocorra algum erro durante a exclusão
            System.out.println("Erro ao excluir o garanhão: " + e.getMessage()); // Log no backend
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao excluir: " + e.getMessage());
        }
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
