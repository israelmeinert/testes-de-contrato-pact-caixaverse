package br.com.horacerta.ferias.clients;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.time.YearMonth;

import com.fasterxml.jackson.databind.ObjectMapper;
import br.com.horacerta.ferias.dtos.ApuracaoClienteDto;

public class ApuracaoClient {
    private final HttpClient http;
    private final URI baseUri;
    private final ObjectMapper mapper;

    public ApuracaoClient(HttpClient http, URI baseUri, ObjectMapper mapper) {
        this.http = http;
        this.baseUri = baseUri;
        this.mapper = mapper;
    }

    public ApuracaoClienteDto consultar(String id, String mes) {
        if (!id.matches("F[0-9]{3}") || !mes.matches("[0-9]{4}-[0-9]{2}")) {
            throw new IllegalArgumentException("Use código FNNN e competência YYYY-MM");
        }
        YearMonth.parse(mes);
        var url = baseUri.resolve("/apuracoes/" + id + "/" + mes);
        var request = HttpRequest.newBuilder(url).timeout(Duration.ofSeconds(5))
                .header("Accept", "application/json").GET().build();

        try {
            var response = http.send(request, BodyHandlers.ofString());
            if (response.statusCode() == 404) {
                throw new ApuracaoNaoEncontradaException(id, mes);
            }
            if (response.statusCode() != 200) {
                throw new IntegracaoException(response.statusCode());
            }
            var resumo = mapper.readValue(response.body(), ApuracaoClienteDto.class);
            if (resumo == null) {
                throw new IntegracaoException("Resposta JSON nula da apuração", null);
            }
            return resumo;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IntegracaoException("Consulta de apuração interrompida", e);
        } catch (IOException e) {
            throw new IntegracaoException("Falha de transporte ou JSON inválido na apuração", e);
        }
    }
}
