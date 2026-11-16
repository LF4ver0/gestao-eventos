package org.lfavero.domain.repositories;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import org.lfavero.domain.entities.Evento;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class EventoRepository implements PanacheRepositoryBase<Evento, Integer> {
    public List<Evento> buscar(Integer instituicaoId, Boolean ativo) {

        List<String> filtros = new ArrayList<>();
        Map<String, Object> params = new HashMap<>();

        if (instituicaoId != null) {
            filtros.add("instituicao.id = :instituicaoId");
            params.put("instituicaoId", instituicaoId);
        }

        if (ativo != null) {
            filtros.add("ativo = :ativo");
            params.put("ativo", ativo);
        }

        String query = filtros.isEmpty()
                ? "1 = 1"
                : String.join(" and ", filtros);

        return list(query, Sort.by("dataInicial").and("id"), params);
    }

    public long contarPorInstituicao(Integer instituicaoId) {
        return count("instituicao.id", instituicaoId);
    }

    public int ativarVigentes(LocalDate hoje) {
        return update(
                "ativo = true where ativo = false " +
                        "and dataInicial <= :hoje " +
                        "and dataFinal >= :hoje",
                Map.of("hoje", hoje)
        );
    }

    public int inativarForaDaVigencia(LocalDate hoje) {
        return update(
                "ativo = false where ativo = true " +
                        "and (dataFinal < :hoje or dataInicial > :hoje)",
                Map.of("hoje", hoje)
        );
    }
}
