export interface OcrResultadoDTO {
    textoCompleto?: string;
    pessoas: string[];
    locais: string[];
    eventos: string[];
    organizacoes: string[];
    assuntos: string[];
    datasMencionadas: string[];
    tipoDocumento?: string;
}

export interface DocumentoDTO {
    id?: number;
    descricao: string;
    urlImagem?: string;
    urlThumbnail?: string;
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
    imagensUrls?: string[];
    thumbnailsUrls?: string[];
    pessoas?: string[];
    locais?: string[];
    eventos?: string[];
    organizacoes?: string[];
    preUploadedFiles?: string[];
    ocrResultadosImagem?: { [url: string]: OcrResultadoDTO };
}
