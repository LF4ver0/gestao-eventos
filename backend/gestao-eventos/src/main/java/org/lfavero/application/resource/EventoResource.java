package org.lfavero.application.resource;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.lfavero.application.dto.request.EventoRequest;
import org.lfavero.application.dto.response.EventoResponse;
import org.lfavero.application.services.EventoService;
import org.lfavero.application.services.VigenciaService;

import java.net.URI;
import java.util.List;

@Path("/api/eventos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EventoResource {
    @Inject
    EventoService service;

    @Inject
    VigenciaService vigencia;

    @GET
    public List<EventoResponse> listar(@QueryParam("instituicaoId") Integer instituicaoId,
                                       @QueryParam("ativo") Boolean ativo) {
        return service.listar(instituicaoId, ativo);
    }

    @GET
    @Path("/{id}")
    public EventoResponse buscar(@PathParam("id") Integer id) {
        return service.buscar(id);
    }

    @POST
    public Response criar(@Valid EventoRequest req) {
        EventoResponse criado = service.criar(req);
        return Response.created(URI.create("/api/eventos/" + criado.id())).entity(criado).build();
    }

    @PUT
    @Path("/{id}")
    public EventoResponse atualizar(@PathParam("id") Integer id, @Valid EventoRequest req) {
        return service.atualizar(id, req);
    }

    @DELETE
    @Path("/{id}")
    public Response excluir(@PathParam("id") Integer id) {
        service.excluir(id);
        return Response.noContent().build();
    }

    @POST
    @Path("/sincronizar-vigencia")
    public VigenciaService.Resultado sincronizarVigencia() {
        return vigencia.sincronizar();
    }
}
