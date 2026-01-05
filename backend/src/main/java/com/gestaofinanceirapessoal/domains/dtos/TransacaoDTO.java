package com.gestaofinanceirapessoal.domains.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.gestaofinanceirapessoal.domains.Transacao;
import com.gestaofinanceirapessoal.domains.enums.TipoTransacao;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransacaoDTO {
    private Long id;
    private TipoTransacao tipo;
    private BigDecimal valor;
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime data;
    private String descricao;
    private Long usuarioId;

    public TransacaoDTO() {}

    public TransacaoDTO(Transacao t) {
        this.id = t.getId();
        this.tipo = t.getTipo();
        this.valor = t.getValor();
        this.data = t.getData();
        this.descricao = t.getDescricao();
        this.usuarioId = t.getUsuario() != null ? t.getUsuario().getId() : null;
    }

    // Getters/Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public TipoTransacao getTipo() { return tipo; }
    public void setTipo(TipoTransacao tipo) { this.tipo = tipo; }

    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }

    public LocalDateTime getData() { return data; }
    public void setData(LocalDateTime data) { this.data = data; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
}
