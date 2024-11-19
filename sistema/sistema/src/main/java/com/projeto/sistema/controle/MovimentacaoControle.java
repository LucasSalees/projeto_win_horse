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

    @GetMapping("/administrativo/movimentacoes/cadastro")
    public ModelAndView cadastrar(Movimentacao movimentacao) {
        ModelAndView mv = new ModelAndView("/administrativo/movimentacoes/cadastro");
        mv.addObject("movimentacao", movimentacao);
        mv.addObject("listarGaranhoes", garanhaoRepositorio.findAll()); // Lista de garanhões
        return mv;
    }

    @GetMapping("/administrativo/movimentacoes/listar")
    public ModelAndView listarMovimentacoes() {
        ModelAndView mv = new ModelAndView("administrativo/movimentacoes/lista");
        List<Movimentacao> listaMovimentacoes = movimentacaoRepositorio.findAll();
        System.out.println("Quantidade de movimentações encontradas: " + listaMovimentacoes.size());
        mv.addObject("listaMovimentacoes", listaMovimentacoes);
        return mv;
    }

    // Editar uma movimentação específica pelo ID
    @GetMapping("/editarMovimentacao/{id_movimentacao}")
    public ModelAndView editar(@PathVariable("id_movimentacao") Long id_movimentacao) {
        Optional<Movimentacao> movimentacao = movimentacaoRepositorio.findById(id_movimentacao);
        return cadastrar(movimentacao.orElse(new Movimentacao()));
    }

    @PostMapping("/salvarMovimentacao")
    public ModelAndView salvarMovimentacao(Movimentacao movimentacao) {
        Garanhao garanhao = garanhaoRepositorio.findById(movimentacao.getGaranhao().getId_garanhao())
                .orElseThrow(() -> new RuntimeException("Garanhão não encontrado com o ID " + movimentacao.getGaranhao().getId_garanhao()));

        if (movimentacao.getQuantidade() > 0) {
            int saldoAtual = garanhao.getSaldo_atual_palhetas();
            int novaQuantidade = saldoAtual - movimentacao.getQuantidade();

            if (novaQuantidade < 0) {
                throw new RuntimeException("Saldo insuficiente de palhetas para realizar a movimentação.");
            }

            garanhao.setSaldo_atual_palhetas(novaQuantidade);
            garanhaoRepositorio.save(garanhao);
        }

        // Preencher os novos campos na movimentação
        movimentacao.setNome_garanhao(garanhao.getNome_garanhao());
        movimentacao.setData_movimentacao(LocalDateTime.now());

        movimentacaoRepositorio.save(movimentacao);

        return new ModelAndView("redirect:/administrativo/movimentacoes/listar");
    }


    // Remover uma movimentação pelo ID
    @GetMapping("/removerMovimentacao/{id_movimentacao}")
    public ModelAndView remover(@PathVariable("id_movimentacao") Long id_movimentacao) {
        movimentacaoRepositorio.deleteById(id_movimentacao);
        return listarMovimentacoes();
    }
}
