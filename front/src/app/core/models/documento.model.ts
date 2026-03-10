export interface DocumentoDTO {
    id?: number;
    titulo: string;
    descricao: string;
    urlImagem?: string;
    conteudoOcr?: string;
    dataDigitalizacao?: string;
    status?: string;
    usuarioId: number;
    usuarioNome?: string;
}
