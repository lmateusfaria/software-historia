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

export interface OcrStatusDTO {
    status: string;
    mensagem: string;
    documentoId: number;
    imagemUrl: string;
}

export interface DocumentoDTO {
    id?: number;
    descricao: string;
    urlImagem?: string;
    urlThumbnail?: string;
    urlPreview?: string;
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
    previewsUrls?: string[];
    pessoas?: string[];
    locais?: string[];
    eventos?: string[];
    organizacoes?: string[];
    preUploadedFiles?: string[];
    ocrResultadosImagem?: { [url: string]: OcrResultadoDTO };
}

export interface ImagemBuscaDTO {
    documentoId: number;
    imagemUrl: string;
    urlThumbnail: string;
    urlPreview: string;
    indice: number;
    textoExtraido: string;
    pessoas: string[];
    locais: string[];
    eventos: string[];
    organizacoes: string[];
    assuntos: string[];
}
