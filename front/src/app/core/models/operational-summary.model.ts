export interface OperationalSummaryDTO {
  totalDocumentos: number;
  aguardandoAprovacao: number;
  processando: number;
  pendenteOcr: number;
  erro: number;
  totalUsuarios: number;
  professores: number;
  alunos: number;
  pesquisadores: number;
  checkedAt: string;
}
