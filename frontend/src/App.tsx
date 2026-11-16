import { useState } from 'react';
import EventosPage from './pages/EventosPage';
import InstituicoesPage from './pages/InstituicoesPage';

type Aba = 'eventos' | 'instituicoes';

export default function App() {
  const [aba, setAba] = useState<Aba>('eventos');

  return (
    <div className="container">
      <header className="topo">
        <h1>Gestão de Eventos</h1>
      </header>
      <div className="barra">
        <nav>
          <button className={aba === 'eventos' ? 'tab ativa' : 'tab'} onClick={() => setAba('eventos')}>
            Eventos
          </button>
          <button
            className={aba === 'instituicoes' ? 'tab ativa' : 'tab'}
            onClick={() => setAba('instituicoes')}
          >
            Instituições
          </button>
        </nav>
        <h2>{aba === 'eventos' ? 'Eventos' : 'Instituições'}</h2>
      </div>
      {/* força remontar ao trocar de aba, recarregando a lista atualizada */}
      <main>{aba === 'eventos' ? <EventosPage key="e" /> : <InstituicoesPage key="i" />}</main>
    </div>
  );
}
