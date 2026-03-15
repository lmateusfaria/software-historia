package br.com.unifef.biblioteca.domains.dtos;

import br.com.unifef.biblioteca.domains.enums.Perfil;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.br.CPF;

import java.io.Serializable;

public class UsuarioUpdateDTO implements Serializable {
    private static final long serialVersionUID = 1L;

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

    private Perfil perfil;

    private Boolean podeCadastrar = false;

    private String senha; // Opcional na atualização

    public UsuarioUpdateDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Perfil getPerfil() { return perfil; }
    public void setPerfil(Perfil perfil) { this.perfil = perfil; }

    public Boolean getPodeCadastrar() { return podeCadastrar; }
    public void setPodeCadastrar(Boolean podeCadastrar) { this.podeCadastrar = podeCadastrar; }

    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }
}
