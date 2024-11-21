package com.projeto.sistema.controle;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import com.projeto.sistema.modelos.Garanhao;
import com.projeto.sistema.modelos.Movimentacao;
import com.projeto.sistema.repositorios.GaranhaoRepositorio;
import com.projeto.sistema.repositorios.MovimentacaoRepositorio;

@Controller
public class MovimentacaoControle {

    @Autowired
    private MovimentacaoRepositorio movimentacaoRepositorio;

    @Autowired
    private GaranhaoRepositorio garanhaoRepositorio;

    // Página de cadastro de movimentação
    @GetMapping("/administrativo/movimentacoes/cadastro")
    public ModelAndView cadastrar(Movimentacao movimentacao) {
        ModelAndView mv = new ModelAndView("/administrativo/movimentacoes/cadastro");
        mv.addObject("movimentacao", movimentacao);
        mv.addObject("listarGaranhoes", garanhaoRepositorio.findAll()); // Lista de garanhões
        return mv;
    }

    // Listagem de todas as movimentações
    @GetMapping("/administrativo/movimentacoes/listar")
    public ModelAndView listarMovimentacoes() {
        ModelAndView mv = new ModelAndView("administrativo/movimentacoes/lista");
        List<Movimentacao> listaMovimentacoes = movimentacaoRepositorio.findAll();
        mv.addObject("listaMovimentacoes", listaMovimentacoes);
        return mv;
    }

    // Editar uma movimentação específica pelo ID
    @GetMapping("/editarMovimentacao/{id_movimentacao}")
    public ModelAndView editar(@PathVariable("id_movimentacao") Long id_movimentacao) {
        Optional<Movimentacao> movimentacao = movimentacaoRepositorio.findById(id_movimentacao);
        return cadastrar(movimentacao.orElse(new Movimentacao()));
    }

 // Salvar uma nova movimentação
    @PostMapping("/salvarMovimentacao")
    public ModelAndView salvarMovimentacao(Movimentacao movimentacao) {
        // Buscar o garanhão pelo ID
        Garanhao garanhao = garanhaoRepositorio.findById(movimentacao.getGaranhao().getId_garanhao())
                .orElseThrow(() -> new RuntimeException("Garanhão não encontrado com o ID " + movimentacao.getGaranhao().getId_garanhao()));

        // Verificar se a movimentação é válida
        if (movimentacao.getQuantidade() > 0) {
            int saldoAtual = garanhao.getSaldo_atual_palhetas();
            int novaQuantidade = saldoAtual - movimentacao.getQuantidade();

            if (novaQuantidade < 0) {
                throw new RuntimeException("Saldo insuficiente de palhetas para realizar a movimentação.");
            }

            // Atualizar o saldo do garanhão
            garanhao.setSaldo_atual_palhetas(novaQuantidade);
            garanhaoRepositorio.save(garanhao);  // Salvar a atualização no saldo do garanhão
        }

        // Preencher os novos campos na movimentação
        movimentacao.setNome_garanhao(garanhao.getNome_garanhao());
        movimentacao.setData_movimentacao(LocalDateTime.now());

        // Atribuir o valor do destino na movimentação
        String destino = movimentacao.getDestino(); // Verifica qual destino foi escolhido
        movimentacao.setDestino(destino); // Garantir que o destino seja salvo

        // Salvar movimentação no banco, sempre que o destino for diferente de null ou vazio
        movimentacaoRepositorio.save(movimentacao);

        // Lógica para o redirecionamento para a lista de movimentações, independentemente do destino
        return new ModelAndView("redirect:/administrativo/movimentacoes/listar");
    }


    // Formulário para venda
    @GetMapping("/administrativo/movimentacoes/vendaFormulario")
    public ModelAndView vendaFormulario() {
        ModelAndView mv = new ModelAndView("/administrativo/movimentacoes/vendaFormulario");
        mv.addObject("venda", new Object()); // Passar os dados necessários para o formulário de venda
        return mv;
    }

    // Formulário para uso interno
    @GetMapping("/administrativo/movimentacoes/usoInternoFormulario")
    public ModelAndView usoInternoFormulario() {
        ModelAndView mv = new ModelAndView("/administrativo/movimentacoes/usoInternoFormulario");
        mv.addObject("usoInterno", new Object()); // Passar os dados necessários para o formulário de uso interno
        return mv;
    }

    // Formulário para transferência
    @GetMapping("/administrativo/movimentacoes/transferenciaFormulario")
    public ModelAndView transferenciaFormulario() {
        ModelAndView mv = new ModelAndView("/administrativo/movimentacoes/transferenciaFormulario");
        mv.addObject("transferencia", new Object()); // Passar os dados necessários para o formulário de transferência
        return mv;
    }

    // Remover uma movimentação pelo ID
    @GetMapping("/removerMovimentacao/{id_movimentacao}")
    public ModelAndView remover(@PathVariable("id_movimentacao") Long id_movimentacao) {
        movimentacaoRepositorio.deleteById(id_movimentacao);
        return listarMovimentacoes(); // Após remover, exibe a lista de movimentações
    }
}
