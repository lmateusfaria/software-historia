package br.com.unifef.biblioteca.domains;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "imagem_ocr_resultado")
public class ImagemOcrResultado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "documento_id")
    private Long documentoId;

    @Column(name = "imagem_url")
    private String imagemUrl;

    private Integer indice;

    @Column(columnDefinition = "TEXT")
    private String textoExtraido;

    @Column(columnDefinition = "TEXT")
    private String pessoasExtraidas;

    @Column(columnDefinition = "TEXT")
    private String locaisExtraidos;

    @Column(columnDefinition = "TEXT")
    private String eventosExtraidos;

    @Column(columnDefinition = "TEXT")
    private String organizacoesExtraidas;

    @Column(columnDefinition = "TEXT")
    private String assuntosExtraidos;

    @Column(columnDefinition = "TEXT")
    private String datasMencionadas;

    private String tipoDocumento;

    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime dataExtracao = LocalDateTime.now();

    public ImagemOcrResultado() {}

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getDocumentoId() { return documentoId; }
    public void setDocumentoId(Long documentoId) { this.documentoId = documentoId; }

    public String getImagemUrl() { return imagemUrl; }
    public void setImagemUrl(String imagemUrl) { this.imagemUrl = imagemUrl; }

    public Integer getIndice() { return indice; }
    public void setIndice(Integer indice) { this.indice = indice; }

    public String getTextoExtraido() { return textoExtraido; }
    public void setTextoExtraido(String textoExtraido) { this.textoExtraido = textoExtraido; }

    public String getPessoasExtraidas() { return pessoasExtraidas; }
    public void setPessoasExtraidas(String pessoasExtraidas) { this.pessoasExtraidas = pessoasExtraidas; }

    public String getLocaisExtraidos() { return locaisExtraidos; }
    public void setLocaisExtraidos(String locaisExtraidos) { this.locaisExtraidos = locaisExtraidos; }

    public String getEventosExtraidos() { return eventosExtraidos; }
    public void setEventosExtraidos(String eventosExtraidos) { this.eventosExtraidos = eventosExtraidos; }

    public String getOrganizacoesExtraidas() { return organizacoesExtraidas; }
    public void setOrganizacoesExtraidas(String organizacoesExtraidas) { this.organizacoesExtraidas = organizacoesExtraidas; }

    public String getAssuntosExtraidos() { return assuntosExtraidos; }
    public void setAssuntosExtraidos(String assuntosExtraidos) { this.assuntosExtraidos = assuntosExtraidos; }

    public String getDatasMencionadas() { return datasMencionadas; }
    public void setDatasMencionadas(String datasMencionadas) { this.datasMencionadas = datasMencionadas; }

    public String getTipoDocumento() { return tipoDocumento; }
    public void setTipoDocumento(String tipoDocumento) { this.tipoDocumento = tipoDocumento; }

    public LocalDateTime getDataExtracao() { return dataExtracao; }
    public void setDataExtracao(LocalDateTime dataExtracao) { this.dataExtracao = dataExtracao; }
}
