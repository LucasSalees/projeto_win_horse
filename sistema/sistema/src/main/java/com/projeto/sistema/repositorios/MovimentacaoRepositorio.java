package com.projeto.sistema.repositorios;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.projeto.sistema.modelos.Movimentacao;

@Repository
public interface MovimentacaoRepositorio extends JpaRepository<Movimentacao, Long> {
	
}
