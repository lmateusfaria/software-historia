package br.com.unifef.biblioteca.domains;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import br.com.unifef.biblioteca.domains.dtos.UsuarioDTO;
import br.com.unifef.biblioteca.domains.enums.Perfil;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String cpf;

    private String nome;

    @Column(unique = true, nullable = false)
    private String email;

    @JsonIgnore
    private String senha;

    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate dataCriacao = LocalDate.now();

    @Enumerated(EnumType.STRING)
    private Perfil perfil;

    private Boolean podeCadastrar = false;

    public Usuario() {
    }

    public Usuario(Long id, String cpf, String nome, String email, String senha, LocalDate dataCriacao) {
        this.id = id;
        this.cpf = cpf;
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.dataCriacao = dataCriacao;
    }

    public Usuario(Long id, String cpf, String nome, String email, String senha, LocalDate dataCriacao, Perfil perfil) {
        this.id = id;
        this.cpf = cpf;
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.dataCriacao = dataCriacao;
        this.perfil = perfil;
        this.podeCadastrar = false;
    }

    public Usuario(Long id, String cpf, String nome, String email, String senha, LocalDate dataCriacao, Perfil perfil, Boolean podeCadastrar) {
        this.id = id;
        this.cpf = cpf;
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.dataCriacao = dataCriacao;
        this.perfil = perfil;
        this.podeCadastrar = podeCadastrar;
    }

    public Usuario(UsuarioDTO usuarioDTO) {
        this.id = usuarioDTO.getId();
        this.cpf = usuarioDTO.getCpf();
        this.nome = usuarioDTO.getNome();
        this.email = usuarioDTO.getEmail();
        this.senha = usuarioDTO.getSenha();
        this.dataCriacao = usuarioDTO.getDataCriacao();
        this.perfil = usuarioDTO.getPerfil();
        this.podeCadastrar = usuarioDTO.getPodeCadastrar();
    }


    // Getters e Setters --------------------------

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
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

    public LocalDate getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDate dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public Perfil getPerfil() {
        return perfil;
    }

    public void setPerfil(Perfil perfil) {
        this.perfil = perfil;
    }

    public Boolean getPodeCadastrar() {
        return podeCadastrar;
    }

    public void setPodeCadastrar(Boolean podeCadastrar) {
        this.podeCadastrar = podeCadastrar;
    }
}

