package com.projeto.sistema.repositorios;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.projeto.sistema.modelos.Garanhao;

@Repository
public interface GaranhaoRepositorio extends JpaRepository<Garanhao, Long> {
    
}
