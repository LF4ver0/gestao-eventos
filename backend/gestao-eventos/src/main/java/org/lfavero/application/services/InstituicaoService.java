package org.lfavero.application.services;

import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.lfavero.application.dto.request.InstituicaoRequest;
import org.lfavero.application.dto.response.InstituicaoResponse;
import org.lfavero.application.exception.ApiException;
import org.lfavero.domain.entities.Instituicao;
import org.lfavero.domain.repositories.EventoRepository;
import org.lfavero.domain.repositories.InstituicaoRepository;

import java.util.List;

@ApplicationScoped
public class InstituicaoService {

    @Inject
    InstituicaoRepository instituicoes;

    @Inject
    EventoRepository eventos;

    @Transactional
    public List<InstituicaoResponse> listar() {
        return instituicoes.listAll(Sort.by("nome")).stream().map(InstituicaoResponse::de).toList();
    }

    @Transactional
    public InstituicaoResponse buscar(Integer id) {
        return InstituicaoResponse.de(obter(id));
    }

    @Transactional
    public InstituicaoResponse criar(InstituicaoRequest req) {
        Instituicao instituicao = new Instituicao();
        aplicar(instituicao, req);
        instituicoes.persist(instituicao);
        return InstituicaoResponse.de(instituicao);
    }

    @Transactional
    public InstituicaoResponse atualizar(Integer id, InstituicaoRequest req) {
        Instituicao i = obter(id);
        aplicar(i, req);
        return InstituicaoResponse.de(i);
    }

    @Transactional
    public void excluir(Integer id) {
        Instituicao i = obter(id);
        if (eventos.contarPorInstituicao(id) > 0) {
            throw ApiException.conflito("Não é possível excluir a instituição: ela possui eventos cadastrados.");
        }
        instituicoes.delete(i);
    }

    public Instituicao obter(Integer id) {
        Instituicao i = instituicoes.findById(id);
        if (i == null) {
            throw ApiException.naoEncontrado("Instituição " + id + " não encontrada.");
        }
        return i;
    }

    private void aplicar(Instituicao i, InstituicaoRequest req) {
        i.atualizarDados(req.nome(), req.tipo());
    }
}
