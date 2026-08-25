package br.com.unifef.biblioteca.domains.dtos;

public class OperationalSummaryDTO {

    private long totalDocumentos;
    private long aguardandoAprovacao;
    private long processando;
    private long pendenteOcr;
    private long erro;
    private long totalUsuarios;
    private long professores;
    private long alunos;
    private long pesquisadores;
    private String checkedAt;
    private SystemHealthDTO systemHealth;

    public long getTotalDocumentos() {
        return totalDocumentos;
    }

    public void setTotalDocumentos(long totalDocumentos) {
        this.totalDocumentos = totalDocumentos;
    }

    public long getAguardandoAprovacao() {
        return aguardandoAprovacao;
    }

    public void setAguardandoAprovacao(long aguardandoAprovacao) {
        this.aguardandoAprovacao = aguardandoAprovacao;
    }

    public long getProcessando() {
        return processando;
    }

    public void setProcessando(long processando) {
        this.processando = processando;
    }

    public long getPendenteOcr() {
        return pendenteOcr;
    }

    public void setPendenteOcr(long pendenteOcr) {
        this.pendenteOcr = pendenteOcr;
    }

    public long getErro() {
        return erro;
    }

    public void setErro(long erro) {
        this.erro = erro;
    }

    public long getTotalUsuarios() {
        return totalUsuarios;
    }

    public void setTotalUsuarios(long totalUsuarios) {
        this.totalUsuarios = totalUsuarios;
    }

    public long getProfessores() {
        return professores;
    }

    public void setProfessores(long professores) {
        this.professores = professores;
    }

    public long getAlunos() {
        return alunos;
    }

    public void setAlunos(long alunos) {
        this.alunos = alunos;
    }

    public long getPesquisadores() {
        return pesquisadores;
    }

    public void setPesquisadores(long pesquisadores) {
        this.pesquisadores = pesquisadores;
    }

    public String getCheckedAt() {
        return checkedAt;
    }

    public void setCheckedAt(String checkedAt) {
        this.checkedAt = checkedAt;
    }

    public SystemHealthDTO getSystemHealth() {
        return systemHealth;
    }

    public void setSystemHealth(SystemHealthDTO systemHealth) {
        this.systemHealth = systemHealth;
    }
}
