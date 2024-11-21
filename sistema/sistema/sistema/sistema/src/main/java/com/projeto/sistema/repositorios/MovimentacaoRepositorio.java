package com.projeto.sistema.repositorios;

import org.springframework.data.jpa.repository.JpaRepository;
import com.projeto.sistema.modelos.Movimentacao;

public interface MovimentacaoRepositorio extends JpaRepository<Movimentacao, Long> {
	
}
