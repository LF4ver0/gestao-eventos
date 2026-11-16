package org.lfavero.application.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.lfavero.application.dto.request.EventoRequest;
import org.lfavero.application.dto.response.EventoResponse;
import org.lfavero.application.exception.ApiException;
import org.lfavero.domain.entities.Evento;
import org.lfavero.domain.entities.Instituicao;
import org.lfavero.domain.repositories.EventoRepository;

import java.util.List;

@ApplicationScoped
public class EventoService {

    @Inject
    EventoRepository eventos;

    @Inject
    InstituicaoService instituicoes;

    @Inject
    VigenciaService vigencia;

    @Transactional
    public List<EventoResponse> listar(Integer instituicaoId, Boolean ativo) {
        return eventos.buscar(instituicaoId, ativo).stream().map(EventoResponse::de).toList();
    }

    @Transactional
    public EventoResponse buscar(Integer id) {
        return EventoResponse.de(obter(id));
    }

    @Transactional
    public EventoResponse criar(EventoRequest req) {
        Evento e = new Evento();
        validarInicioNaoPassado(req);
        aplicar(e, req);
        eventos.persist(e);
        return EventoResponse.de(e);
    }

    @Transactional
    public EventoResponse atualizar(Integer id, EventoRequest req) {
        Evento e = obter(id);

        if (!req.dataInicial().equals(e.getDataInicial())) {
            validarInicioNaoPassado(req);
        }
        aplicar(e, req);
        return EventoResponse.de(e);
    }

    @Transactional
    public void excluir(Integer id) {
        eventos.delete(obter(id));
    }

    private Evento obter(Integer id) {
        Evento e = eventos.findById(id);
        if (e == null) {
            throw ApiException.naoEncontrado("Evento " + id + " não encontrado.");
        }
        return e;
    }

    private void validarInicioNaoPassado(EventoRequest req) {
        if (req.dataInicial().isBefore(vigencia.hoje())) {
            throw ApiException.validacao("dataInicial", "A data inicial não pode ser anterior à data atual");
        }
    }

    private void aplicar(Evento e, EventoRequest req) {
        Instituicao instituicao = instituicoes.obter(req.instituicaoId());

        boolean ativo = vigencia.estaVigenteHoje(
                req.dataInicial(),
                req.dataFinal()
        );

        e.atualizarDados(
                req.nome(),
                req.dataInicial(),
                req.dataFinal(),
                instituicao,
                ativo
        );
    }
}