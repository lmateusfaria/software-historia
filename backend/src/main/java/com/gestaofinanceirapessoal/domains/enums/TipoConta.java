package com.gestaofinanceirapessoal.domains.enums;

public enum TipoConta {

    CONTA_CORRENTE(0, "Conta Corrente"),
    CONTA_INVESTIMENTO(1, "Conta Investimento"),
    CONTA_CREDITO(2, "Conta Crédito"),
    CONTA_ALIMENTACAO(3, "Conta Alimentação"),
    CONTA_POUPANCA(4, "Conta Poupança");

    private Integer id;
    private String descricao;

    TipoConta(Integer id, String descricao) {
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

    public static TipoConta toEnum(Integer id) {
        if (id == null) return null;
        for (TipoConta x : TipoConta.values()) {
            if (id.equals(x.getId())) {
                return x;
            }
        }
        throw new IllegalArgumentException("Tipo de Conta inválido");
    }
}
