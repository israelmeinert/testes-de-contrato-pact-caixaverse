package br.com.horacerta.integracao.resources;

import java.time.DateTimeException;

import br.com.horacerta.integracao.clients.ApuracaoClient;
import br.com.horacerta.integracao.clients.ApuracaoNaoEncontradaException;
import br.com.horacerta.integracao.clients.IntegracaoException;
import br.com.horacerta.integracao.dtos.ApuracaoClienteDto;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;

@Path("/integracoes")
@Produces(MediaType.APPLICATION_JSON)
public class IntegracaoClienteResource {
    @Inject
    ApuracaoClient client;

    @GET
    @Path("/{id}/{competencia}")
    public ApuracaoClienteDto consultar(@PathParam("id") String id, @PathParam("competencia") String competencia) {
        try {
            return client.consultar(id, competencia);
        } catch (ApuracaoNaoEncontradaException e) {
            throw new NotFoundException(e);
        } catch (IllegalArgumentException | DateTimeException e) {
            throw new BadRequestException(e);
        } catch (IntegracaoException e) {
            throw new WebApplicationException(e, 502);
        }
    }
}
