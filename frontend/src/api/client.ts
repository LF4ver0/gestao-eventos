import type { Evento, EventoForm, Instituicao, InstituicaoForm } from './types';

export class ApiError extends Error {
  status: number;
  campos: Record<string, string>;

  constructor(status: number, mensagem: string, campos: Record<string, string> = {}) {
    super(mensagem);
    this.status = status;
    this.campos = campos;
  }
}

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  let res: Response;
  try {
    res = await fetch(`/api${path}`, {
      ...init,
      headers: { 'Content-Type': 'application/json', ...(init?.headers ?? {}) },
    });
  } catch {
    throw new ApiError(0, 'Não foi possível conectar ao servidor.');
  }

  if (!res.ok) {
    let mensagem = `Erro ${res.status}`;
    const campos: Record<string, string> = {};
    try {
      const corpo = await res.json();
      if (corpo?.mensagem) mensagem = corpo.mensagem;
      for (const e of corpo?.erros ?? []) campos[e.campo] = e.mensagem;
    } catch {
    }
    throw new ApiError(res.status, mensagem, campos);
  }

  if (res.status === 204) return undefined as T;
  return (await res.json()) as T;
}

const json = (body: unknown) => JSON.stringify(body);

export const api = {
  instituicoes: {
    listar: () => request<Instituicao[]>('/instituicoes'),
    criar: (f: InstituicaoForm) => request<Instituicao>('/instituicoes', { method: 'POST', body: json(f) }),
    atualizar: (id: number, f: InstituicaoForm) =>
      request<Instituicao>(`/instituicoes/${id}`, { method: 'PUT', body: json(f) }),
    excluir: (id: number) => request<void>(`/instituicoes/${id}`, { method: 'DELETE' }),
  },
  eventos: {
    listar: (filtros: { instituicaoId?: number; ativo?: boolean } = {}) => {
      const qs = new URLSearchParams();
      if (filtros.instituicaoId !== undefined) qs.set('instituicaoId', String(filtros.instituicaoId));
      if (filtros.ativo !== undefined) qs.set('ativo', String(filtros.ativo));
      const s = qs.toString();
      return request<Evento[]>(`/eventos${s ? `?${s}` : ''}`);
    },
    criar: (f: EventoForm) => request<Evento>('/eventos', { method: 'POST', body: json(f) }),
    atualizar: (id: number, f: EventoForm) =>
      request<Evento>(`/eventos/${id}`, { method: 'PUT', body: json(f) }),
    excluir: (id: number) => request<void>(`/eventos/${id}`, { method: 'DELETE' }),
  },
};
