package br.com.unifef.biblioteca.domains.enums;

public enum Perfil {
    PROFESSOR(1, "ROLE_PROFESSOR"),
    ALUNO(2, "ROLE_ALUNO"),
    PESQUISADOR(3, "ROLE_PESQUISADOR");

    private Integer codigo;
    private String descricao;

    Perfil(Integer codigo, String descricao) {
        this.codigo = codigo;
        this.descricao = descricao;
    }

    public Integer getCodigo() {
        return codigo;
    }

    public String getDescricao() {
        return descricao;
    }

    public static Perfil toEnum(Integer cod) {
        if (cod == null) {
            return null;
        }

        for (Perfil x : Perfil.values()) {
            if (cod.equals(x.getCodigo())) {
                return x;
            }
        }

        throw new IllegalArgumentException("Id inválido: " + cod);
    }
}
