package com.projeto.sistema.repositorios;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.projeto.sistema.modelos.Usuario;

@Repository
public interface UsuarioRepositorio extends JpaRepository<Usuario, Long> {
	@Query(value="select * from usuarios where email =  :email and senha = :senha", nativeQuery = true)
	public Usuario Login(String email, String senha);
}
