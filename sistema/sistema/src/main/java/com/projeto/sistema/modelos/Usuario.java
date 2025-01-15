package com.projeto.sistema.modelos;

import java.io.Serializable;
import java.time.LocalDateTime;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "usuarios") // Nome da tabela no banco de dados
public class Usuario implements Serializable {

    private static final long serialVersionUID = 1L; // Controle de versão da serialização

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // ID gerado automaticamente pelo banco
    private Long id_usuario;
    private String nome_usuario;
    private String email;
    private String senha;
    private String tipo;  // Novo campo 'tipo'
	private LocalDateTime data_cadastro = LocalDateTime.now(); // Data e hora do cadastro

    // Construtor com todos os parâmetros necessários
    public Usuario(String nome_usuario, String email, String senha) {
        this.nome_usuario = nome_usuario;
        this.email = email;
        this.senha = senha;
        this.data_cadastro = LocalDateTime.now(); // Data e hora de cadastro
    }

    // Construtor padrão (caso precise)
    public Usuario() {
    }

    // Getters e Setters
    public Long getId_usuario() {
        return id_usuario;
    }

    public void setId_usuario(Long id_usuario) {
        this.id_usuario = id_usuario;
    }

    public String getNome_usuario() {
        return nome_usuario;
    }

    public void setNome_usuario(String nome_usuario) {
        this.nome_usuario = nome_usuario;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public LocalDateTime getData_cadastro() {
        return data_cadastro;
    }

    public void setData_cadastro(LocalDateTime data_cadastro) {
        this.data_cadastro = data_cadastro;
    }
    
    public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	public static boolean isPresent() {
		// TODO Auto-generated method stub
		return false;
	}

	public static Object get() {
		// TODO Auto-generated method stub
		return null;
	}
}
