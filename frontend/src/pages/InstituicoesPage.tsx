import { FormEvent, useCallback, useEffect, useState } from 'react';
import { api, ApiError } from '../api/client';
import { Instituicao, InstituicaoForm, TIPOS_INSTITUICAO } from '../api/types';

const VAZIO: InstituicaoForm = { nome: '', tipo: 'SINGULAR' };

export default function InstituicoesPage() {
  const [lista, setLista] = useState<Instituicao[]>([]);
  const [form, setForm] = useState<InstituicaoForm>(VAZIO);
  const [editandoId, setEditandoId] = useState<number | null>(null);
  const [erroGeral, setErroGeral] = useState<string | null>(null);
  const [erros, setErros] = useState<Record<string, string>>({});
  const [salvando, setSalvando] = useState(false);
  const [carregando, setCarregando] = useState(true);

  const carregar = useCallback(async () => {
    setCarregando(true);
    try {
      setLista(await api.instituicoes.listar());
    } catch (e) {
      setErroGeral((e as Error).message);
    } finally {
      setCarregando(false);
    }
  }, []);

  useEffect(() => {
    void carregar();
  }, [carregar]);

  function limpar() {
    setForm(VAZIO);
    setEditandoId(null);
    setErros({});
    setErroGeral(null);
  }

  async function salvar(ev: FormEvent) {
    ev.preventDefault();
    setErros({});
    setErroGeral(null);

    if (!form.nome.trim()) {
      setErros({ nome: 'O nome é obrigatório' });
      return;
    }

    setSalvando(true);
    try {
      if (editandoId === null) await api.instituicoes.criar(form);
      else await api.instituicoes.atualizar(editandoId, form);
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

  async function excluir(i: Instituicao) {
    if (!window.confirm(`Excluir a instituição "${i.nome}"?`)) return;
    setErroGeral(null);
    try {
      await api.instituicoes.excluir(i.id);
      await carregar();
    } catch (e) {
      setErroGeral((e as Error).message);
    }
  }

  return (
    <section>
      <form className="card" onSubmit={salvar} noValidate>
        <h3>{editandoId === null ? 'Nova instituição' : `Editando #${editandoId}`}</h3>
        <div className="grid">
          <label>
            Nome
            <input
              value={form.nome}
              maxLength={150}
              onChange={(e) => setForm({ ...form, nome: e.target.value })}
            />
            {erros.nome && <span className="erro">{erros.nome}</span>}
          </label>
          <label>
            Tipo
            <select
              value={form.tipo}
              onChange={(e) => setForm({ ...form, tipo: e.target.value as InstituicaoForm['tipo'] })}
            >
              {TIPOS_INSTITUICAO.map((t) => (
                <option key={t.valor} value={t.valor}>
                  {t.rotulo}
                </option>
              ))}
            </select>
            {erros.tipo && <span className="erro">{erros.tipo}</span>}
          </label>
        </div>
        {erroGeral && <div className="banner-erro">{erroGeral}</div>}
        <div className="acoes">
          <button type="submit" disabled={salvando}>
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

      <table>
        <thead>
          <tr>
            <th>ID</th>
            <th>Nome</th>
            <th>Tipo</th>
            <th />
          </tr>
        </thead>
        <tbody>
          {carregando && (
            <tr>
              <td colSpan={4} className="carregando">
                <span className="spinner" />
              </td>
            </tr>
          )}
          {!carregando && lista.length === 0 && (
            <tr>
              <td colSpan={4} className="vazio">
                Nenhuma instituição cadastrada.
              </td>
            </tr>
          )}
          {!carregando && lista.map((i) => (
            <tr key={i.id}>
              <td>{i.id}</td>
              <td>{i.nome}</td>
              <td>{i.tipoDescricao}</td>
              <td className="acoes-linha">
                <button
                  className="secundario"
                  onClick={() => {
                    setEditandoId(i.id);
                    setForm({ nome: i.nome, tipo: i.tipo });
                    setErros({});
                    setErroGeral(null);
                  }}
                >
                  Editar
                </button>
                <button className="perigo" onClick={() => void excluir(i)}>
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
