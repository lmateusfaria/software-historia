package br.com.unifef.biblioteca.domains.enums;

public enum StatusDocumento {
    AGUARDANDO_APROVACAO(1, "Aguardando Aprovação"),
    APROVADO(2, "Aprovado"),
    REJEITADO(3, "Rejeitado"),
    PENDENTE_OCR(4, "Pendente de OCR"),
    PROCESSANDO_OCR(5, "Processando OCR");

    private int cod;
    private String descricao;

    private StatusDocumento(int cod, String descricao) {
        this.cod = cod;
        this.descricao = descricao;
    }

    public int getCod() {
        return cod;
    }

    public String getDescricao() {
        return descricao;
    }

    public static StatusDocumento toEnum(Integer cod) {
        if (cod == null) {
            return null;
        }
        for (StatusDocumento x : StatusDocumento.values()) {
            if (cod.equals(x.getCod())) {
                return x;
            }
        }
        throw new IllegalArgumentException("Id inválido: " + cod);
    }
}
