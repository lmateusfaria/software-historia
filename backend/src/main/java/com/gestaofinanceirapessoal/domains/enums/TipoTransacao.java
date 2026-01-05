package com.gestaofinanceirapessoal.domains.enums;

public enum TipoTransacao {

    ENTRADA(0, "Entrada"),
    SAIDA(1, "Saída");

    private Integer id;
    private String descricao;

    TipoTransacao(Integer id, String descricao) {
        this.id = id;
        this.descricao = descricao;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public static TipoTransacao toEnum(Integer id) {
        if (id == null) return null;
        for (TipoTransacao x : TipoTransacao.values()) {
            if (id.equals(x.getId())) {
                return x;
            }
        }
        throw new IllegalArgumentException("Tipo de Transação inválido");
    }
}
