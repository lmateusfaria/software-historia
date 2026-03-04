package br.com.unifef.biblioteca.domains;

import br.com.unifef.biblioteca.domains.enums.StatusDocumento;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "documentos")
public class Documento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate dataDigitalizacao = LocalDate.now();

    @Enumerated(EnumType.STRING)
    private StatusDocumento status = StatusDocumento.AGUARDANDO_APROVACAO;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario responsavel; // Aluno que escaneou

    @ElementCollection
    @CollectionTable(name = "documento_imagens", joinColumns = @JoinColumn(name = "documento_id"))
    @Column(name = "imagem_url")
    private List<String> imagensUrls = new ArrayList<>();

    @Column(columnDefinition = "TEXT")
    private String conteudoOcr;

    public Documento() {}

    public Documento(Long id, String titulo, String descricao, Usuario responsavel) {
        this.id = id;
        this.titulo = titulo;
        this.descricao = descricao;
        this.responsavel = responsavel;
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public LocalDate getDataDigitalizacao() { return dataDigitalizacao; }
    public void setDataDigitalizacao(LocalDate dataDigitalizacao) { this.dataDigitalizacao = dataDigitalizacao; }

    public StatusDocumento getStatus() { return status; }
    public void setStatus(StatusDocumento status) { this.status = status; }

    public Usuario getResponsavel() { return responsavel; }
    public void setResponsavel(Usuario responsavel) { this.responsavel = responsavel; }

    public List<String> getImagensUrls() { return imagensUrls; }
    public void setImagensUrls(List<String> imagensUrls) { this.imagensUrls = imagensUrls; }

    public String getUrlImagem() {
        return (imagensUrls != null && !imagensUrls.isEmpty()) ? imagensUrls.get(0) : null;
    }

    public void setUrlImagem(String urlImagem) {
        if (this.imagensUrls == null) this.imagensUrls = new ArrayList<>();
        if (urlImagem != null) this.imagensUrls.add(urlImagem);
    }

    public String getConteudoOcr() { return conteudoOcr; }
    public void setConteudoOcr(String conteudoOcr) { this.conteudoOcr = conteudoOcr; }
}

