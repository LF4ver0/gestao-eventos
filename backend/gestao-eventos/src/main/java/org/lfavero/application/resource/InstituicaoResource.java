package org.lfavero.application.resource;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.lfavero.application.dto.request.InstituicaoRequest;
import org.lfavero.application.dto.response.InstituicaoResponse;
import org.lfavero.application.services.InstituicaoService;

import java.net.URI;
import java.util.List;

@Path("/api/instituicoes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class InstituicaoResource {

    @Inject
    InstituicaoService service;

    @GET
    public List<InstituicaoResponse> listar() {
        return service.listar();
    }

    @GET
    @Path("/{id}")
    public InstituicaoResponse buscar(@PathParam("id") Integer id) {
        return service.buscar(id);
    }

    @POST
    public Response criar(@Valid InstituicaoRequest req) {
        InstituicaoResponse criada = service.criar(req);
        return Response.created(URI.create("/api/instituicoes/" + criada.id())).entity(criada).build();
    }

    @PUT
    @Path("/{id}")
    public InstituicaoResponse atualizar(@PathParam("id") Integer id, @Valid InstituicaoRequest req) {
        return service.atualizar(id, req);
    }

    @DELETE
    @Path("/{id}")
    public Response excluir(@PathParam("id") Integer id) {
        service.excluir(id);
        return Response.noContent().build();
    }
}
