package com.gestaofinanceirapessoal.domains.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gestaofinanceirapessoal.domains.Usuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.br.CPF;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class UsuarioDTO {

    private Long id;

    @NotNull(message = "O campo CPF não pode ser nulo")
    @CPF(message = "CPF inválido")
    private String cpf;

    @NotNull(message = "O campo nome não pode ser nulo")
    @NotBlank(message = "O campo nome não pode ser vazio")
    private String nome;

    @NotNull(message = "O campo e-mail não pode ser nulo")
    @NotBlank(message = "O campo e-mail não pode ser vazio")
    @Email(message = "E-mail inválido")
    private String email;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) // ✅ só pode ser enviado, nunca retornado
    @NotNull(message = "O campo senha não pode ser nulo")
    @NotBlank(message = "O campo senha não pode ser vazio")
    private String senha;

    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate dataCriacao = LocalDate.now();

    private List<Long> contasIds = new ArrayList<>();
    private List<Long> centrosCustoIds = new ArrayList<>();

    public UsuarioDTO() {}

    public UsuarioDTO(Usuario usuario) {
        this.id = usuario.getId();
        this.cpf = usuario.getCpf();
        this.nome = usuario.getNome();
        this.email = usuario.getEmail();
        this.senha = usuario.getSenha();
        this.dataCriacao = LocalDate.now();

    }

    // Getters e Setters -------------------

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }

    public LocalDate getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDate dataCriacao) { this.dataCriacao = dataCriacao; }

    public List<Long> getContasIds() {
        return contasIds;
    }

    public void setContasIds(List<Long> contasIds) {
        this.contasIds = contasIds;
    }

    public List<Long> getCentrosCustoIds() {
        return centrosCustoIds;
    }

    public void setCentrosCustoIds(List<Long> centrosCustoIds) {
        this.centrosCustoIds = centrosCustoIds;
    }
}
