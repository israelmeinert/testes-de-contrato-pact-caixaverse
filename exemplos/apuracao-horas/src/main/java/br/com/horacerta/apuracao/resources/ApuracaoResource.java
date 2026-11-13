package br.com.horacerta.apuracao.resources;

import java.time.YearMonth;
import java.time.format.DateTimeParseException;

import br.com.horacerta.apuracao.dtos.ApuracaoResponse;
import br.com.horacerta.apuracao.dtos.SindicatoResponse;
import br.com.horacerta.apuracao.entities.ApuracaoMensal;
import br.com.horacerta.apuracao.services.ApuracaoService;
import br.com.horacerta.apuracao.services.CalculoMensal;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/apuracoes")
@Produces(MediaType.APPLICATION_JSON)
public class ApuracaoResource {
    @Inject
    ApuracaoService service;

    @GET
    @Path("/{funcionarioId}/{competencia}")
    public ApuracaoResponse consultar(@PathParam("funcionarioId") String id, @PathParam("competencia") String mes) {
        return resposta(service.consultar(id, validar(id, mes)));
    }

    @POST
    @Path("/{funcionarioId}/{competencia}/calcular")
    public ApuracaoResponse calcular(@PathParam("funcionarioId") String id, @PathParam("competencia") String mes) {
        return resposta(service.calcular(id, validar(id, mes)));
    }

    private static YearMonth validar(String id, String mes) {
        if (!id.matches("F[0-9]{3}") || !mes.matches("[0-9]{4}-[0-9]{2}")) {
            throw new BadRequestException("Use código FNNN e competência YYYY-MM");
        }
        try {
            return YearMonth.parse(mes);
        } catch (DateTimeParseException e) {
            throw new BadRequestException("Competência inválida");
        }
    }

    private static ApuracaoResponse resposta(ApuracaoMensal a) {
        var sindicato = a.sindicatoCodigo == null ? null : new SindicatoResponse(a.sindicatoCodigo);
        return new ApuracaoResponse(a.funcionarioId, a.competencia,
                CalculoMensal.horas(a.minutosTrabalhados), CalculoMensal.horas(a.minutosExtras),
                a.diasTrabalhados, sindicato);
    }
}
