package br.com.horacerta.ferias.resources;

import java.time.DateTimeException;

import br.com.horacerta.ferias.clients.ApuracaoClient;
import br.com.horacerta.ferias.clients.ApuracaoNaoEncontradaException;
import br.com.horacerta.ferias.clients.IntegracaoException;
import br.com.horacerta.ferias.dtos.ApuracaoClienteDto;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;

@Path("/ferias/frequencia")
@Produces(MediaType.APPLICATION_JSON)
public class ModuloFeriasResource {
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
