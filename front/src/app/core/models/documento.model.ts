export interface DocumentoDTO {
    id?: number;
    descricao: string;
    urlImagem?: string;
    conteudoOcr?: string;
    dataDigitalizacao?: string;
    status?: string;
    usuarioId: number;
    usuarioNome?: string;
    tipo?: string;
    diaDocumento?: number;
    mesDocumento?: number;
    anoDocumento?: number;
    localOrigem?: string;
    edicao?: string;
    marcadores?: string;
}
