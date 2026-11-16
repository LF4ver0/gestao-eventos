export type TipoInstituicao = 'CONFEDERACAO' | 'SINGULAR' | 'CENTRAL' | 'COOPERATIVA';

export const TIPOS_INSTITUICAO: { valor: TipoInstituicao; rotulo: string }[] = [
  { valor: 'CONFEDERACAO', rotulo: 'Confederação' },
  { valor: 'SINGULAR', rotulo: 'Singular' },
  { valor: 'CENTRAL', rotulo: 'Central' },
  { valor: 'COOPERATIVA', rotulo: 'Cooperativa' },
];

export interface Instituicao {
  id: number;
  nome: string;
  tipo: TipoInstituicao;
  tipoDescricao: string;
}

export interface InstituicaoForm {
  nome: string;
  tipo: TipoInstituicao;
}

export interface Evento {
  id: number;
  nome: string;
  dataInicial: string;
  dataFinal: string;
  ativo: boolean;
  instituicao: { id: number; nome: string };
}

export interface EventoForm {
  nome: string;
  dataInicial: string;
  dataFinal: string;
  instituicaoId: number | null;
}
