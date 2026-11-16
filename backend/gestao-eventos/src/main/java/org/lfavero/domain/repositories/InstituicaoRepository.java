package org.lfavero.domain.repositories;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import org.lfavero.domain.entities.Instituicao;

@ApplicationScoped
public class InstituicaoRepository implements PanacheRepositoryBase<Instituicao, Integer> {
}
