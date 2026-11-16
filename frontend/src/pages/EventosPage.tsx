import { FormEvent, useCallback, useEffect, useState } from 'react';
import { api, ApiError } from '../api/client';
import { Evento, EventoForm, Instituicao } from '../api/types';
import { formatarData, hojeIso } from '../utils/datas';

const VAZIO: EventoForm = { nome: '', dataInicial: '', dataFinal: '', instituicaoId: null };

type FiltroStatus = 'todos' | 'ativos' | 'inativos';

function validar(f: EventoForm, dataInicialOriginal: string | null): Record<string, string> {
  const e: Record<string, string> = {};
  if (!f.nome.trim()) e.nome = 'O nome é obrigatório';
  if (!f.instituicaoId) e.instituicaoId = 'Selecione uma instituição';
  if (!f.dataInicial) e.dataInicial = 'A data inicial é obrigatória';
  if (!f.dataFinal) e.dataFinal = 'A data final é obrigatória';
  if (f.dataInicial && f.dataInicial < hojeIso() && f.dataInicial !== dataInicialOriginal) {
    e.dataInicial = 'A data inicial não pode ser anterior à data atual';
  }
  if (f.dataInicial && f.dataFinal && f.dataFinal < f.dataInicial) {
    e.dataFinal = 'A data final deve ser maior ou igual à data inicial';
  }
  return e;
}

export default function EventosPage() {
  const [eventos, setEventos] = useState<Evento[]>([]);
  const [instituicoes, setInstituicoes] = useState<Instituicao[]>([]);
  const [carregandoInstituicoes, setCarregandoInstituicoes] = useState(true);
  const [form, setForm] = useState<EventoForm>(VAZIO);
  const [editandoId, setEditandoId] = useState<number | null>(null);
  const [dataInicialOriginal, setDataInicialOriginal] = useState<string | null>(null);
  const [erros, setErros] = useState<Record<string, string>>({});
  const [erroGeral, setErroGeral] = useState<string | null>(null);
  const [salvando, setSalvando] = useState(false);
  const [carregando, setCarregando] = useState(true);
  const [filtroInst, setFiltroInst] = useState<number | ''>('');
  const [filtroStatus, setFiltroStatus] = useState<FiltroStatus>('todos');

  const carregar = useCallback(async () => {
    setCarregando(true);
    try {
      setEventos(
        await api.eventos.listar({
          instituicaoId: filtroInst === '' ? undefined : filtroInst,
          ativo: filtroStatus === 'todos' ? undefined : filtroStatus === 'ativos',
        }),
      );
    } catch (e) {
      setErroGeral((e as Error).message);
    } finally {
      setCarregando(false);
    }
  }, [filtroInst, filtroStatus]);

  useEffect(() => {
    api.instituicoes
      .listar()
      .then(setInstituicoes)
      .catch((e: Error) => setErroGeral(e.message))
      .finally(() => setCarregandoInstituicoes(false));
  }, []);

  useEffect(() => {
    void carregar();
  }, [carregar]);

  function limpar() {
    setForm(VAZIO);
    setEditandoId(null);
    setDataInicialOriginal(null);
    setErros({});
    setErroGeral(null);
  }

  async function salvar(ev: FormEvent) {
    ev.preventDefault();
    setErroGeral(null);

    const locais = validar(form, dataInicialOriginal);
    setErros(locais);
    if (Object.keys(locais).length > 0) return;

    setSalvando(true);
    try {
      if (editandoId === null) await api.eventos.criar(form);
      else await api.eventos.atualizar(editandoId, form);
      limpar();
      await carregar();
    } catch (e) {
      if (e instanceof ApiError) {
        setErros(e.campos);
        setErroGeral(Object.keys(e.campos).length ? null : e.message);
      }
    } finally {
      setSalvando(false);
    }
  }

  async function excluir(ev: Evento) {
    if (!window.confirm(`Excluir o evento "${ev.nome}"?`)) return;
    try {
      await api.eventos.excluir(ev.id);
      await carregar();
    } catch (e) {
      setErroGeral((e as Error).message);
    }
  }

  const semInstituicao = !carregandoInstituicoes && instituicoes.length === 0;

  return (
    <section>
      <form className="card" onSubmit={salvar} noValidate>
        <h3>{editandoId === null ? 'Novo evento' : `Editando #${editandoId}`}</h3>

        {semInstituicao && (
          <div className="banner-aviso">Cadastre ao menos uma instituição antes de criar eventos.</div>
        )}

        <div className="grid">
          <label className="largo">
            Nome
            <input
              value={form.nome}
              maxLength={150}
              onChange={(e) => setForm({ ...form, nome: e.target.value })}
            />
            {erros.nome && <span className="erro">{erros.nome}</span>}
          </label>

          <label className="largo">
            Instituição
            <select
              value={form.instituicaoId ?? ''}
              onChange={(e) =>
                setForm({ ...form, instituicaoId: e.target.value ? Number(e.target.value) : null })
              }
            >
              <option value="">Selecione…</option>
              {instituicoes.map((i) => (
                <option key={i.id} value={i.id}>
                  {i.nome} ({i.tipoDescricao})
                </option>
              ))}
            </select>
            {erros.instituicaoId && <span className="erro">{erros.instituicaoId}</span>}
          </label>

          <label>
            Data inicial
            <input
              type="date"
              value={form.dataInicial}
              min={hojeIso()}
              onChange={(e) => setForm({ ...form, dataInicial: e.target.value })}
            />
            {erros.dataInicial && <span className="erro">{erros.dataInicial}</span>}
          </label>

          <label>
            Data final
            <input
              type="date"
              value={form.dataFinal}
              min={form.dataInicial || undefined}
              onChange={(e) => setForm({ ...form, dataFinal: e.target.value })}
            />
            {erros.dataFinal && <span className="erro">{erros.dataFinal}</span>}
          </label>
        </div>

        <p className="dica">
          O evento só pode iniciar a partir de hoje. O status (ativo/inativo) é automático: fica ativo entre a
          data inicial e a data final (inclusive) e é inativado ao fim do último dia.
        </p>

        {erroGeral && <div className="banner-erro">{erroGeral}</div>}

        <div className="acoes">
          <button type="submit" disabled={salvando || carregandoInstituicoes || semInstituicao}>
            {editandoId === null ? 'Cadastrar' : 'Salvar'}
          </button>
          {salvando && <span className="spinner pequeno" />}
          {editandoId !== null && (
            <button type="button" className="secundario" onClick={limpar}>
              Cancelar
            </button>
          )}
        </div>
      </form>

      <div className="filtros">
        <label>
          Instituição
          <select
            value={filtroInst}
            onChange={(e) => setFiltroInst(e.target.value ? Number(e.target.value) : '')}
          >
            <option value="">Todas</option>
            {instituicoes.map((i) => (
              <option key={i.id} value={i.id}>
                {i.nome}
              </option>
            ))}
          </select>
        </label>
        <label>
          Status
          <select value={filtroStatus} onChange={(e) => setFiltroStatus(e.target.value as FiltroStatus)}>
            <option value="todos">Todos</option>
            <option value="ativos">Ativos</option>
            <option value="inativos">Inativos</option>
          </select>
        </label>
      </div>

      <table>
        <thead>
          <tr>
            <th>Nome</th>
            <th>Instituição</th>
            <th>Vigência</th>
            <th>Status</th>
            <th />
          </tr>
        </thead>
        <tbody>
          {carregando && (
            <tr>
              <td colSpan={5} className="carregando">
                <span className="spinner" />
              </td>
            </tr>
          )}
          {!carregando && eventos.length === 0 && (
            <tr>
              <td colSpan={5} className="vazio">
                Nenhum evento encontrado.
              </td>
            </tr>
          )}
          {!carregando && eventos.map((ev) => (
            <tr key={ev.id}>
              <td>{ev.nome}</td>
              <td>{ev.instituicao.nome}</td>
              <td>
                {formatarData(ev.dataInicial)} a {formatarData(ev.dataFinal)}
              </td>
              <td>
                <span className={ev.ativo ? 'badge ativo' : 'badge inativo'}>
                  {ev.ativo ? 'Ativo' : 'Inativo'}
                </span>
              </td>
              <td className="acoes-linha">
                <button
                  className="secundario"
                  onClick={() => {
                    setEditandoId(ev.id);
                    setDataInicialOriginal(ev.dataInicial);
                    setForm({
                      nome: ev.nome,
                      dataInicial: ev.dataInicial,
                      dataFinal: ev.dataFinal,
                      instituicaoId: ev.instituicao.id,
                    });
                    setErros({});
                    setErroGeral(null);
                    window.scrollTo({ top: 0, behavior: 'smooth' });
                  }}
                >
                  Editar
                </button>
                <button className="perigo" onClick={() => void excluir(ev)}>
                  Excluir
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </section>
  );
}
