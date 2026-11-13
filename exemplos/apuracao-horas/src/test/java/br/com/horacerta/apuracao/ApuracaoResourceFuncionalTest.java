package br.com.horacerta.apuracao;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class ApuracaoResourceFuncionalTest {
    private static final String RESUMO = "/apuracoes/F001/2026-10";
    private final HttpClient http = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    @Inject ApuracaoFixtures fixtures;
    @TestHTTPResource URL baseUrl;

    @ParameterizedTest
    @CsvSource({"SIND-TI,456,8", "SIND-LOG,420,20", "SEM,456,8"})
    void calculaUnidadeExata(String sindicato, int jornada, int extras) throws Exception {
        fixtures.prepararPontos("SEM".equals(sindicato) ? null : sindicato, jornada);
        assertEquals(404, enviar("GET", RESUMO).statusCode());
        assertEquals(0, fixtures.apuracoes(), "GET não grava");
        var calculado = corpo(enviar("POST", RESUMO + "/calcular"));
        assertHoras("160", calculado, "horasTrabalhadas");
        assertHoras(Integer.toString(extras), calculado, "horasExtras");
        assertEquals(20, calculado.get("diasTrabalhados").intValue());
        if ("SEM".equals(sindicato)) {
            assertTrue(calculado.get("sindicato").isNull());
        } else {
            assertEquals(sindicato, calculado.at("/sindicato/codigo").textValue());
        }
        assertEquals(calculado, corpo(enviar("GET", RESUMO)));
        assertEquals(calculado, corpo(enviar("POST", RESUMO + "/calcular")));
        assertEquals(1, fixtures.apuracoes());
    }

    @Test
    void calculaZeroComCamposPresentes() throws Exception {
        fixtures.preparar(ApuracaoFixtures.AUSENTE);
        var calculado = corpo(enviar("POST", "/apuracoes/F404/2026-10/calcular"));
        assertHoras("0", calculado, "horasTrabalhadas");
        assertHoras("0", calculado, "horasExtras");
        assertTrue(calculado.get("diasTrabalhados").isIntegralNumber());
        assertEquals(0, calculado.get("diasTrabalhados").intValue());
    }

    @Test
    void validaParametrosEAusencia() throws Exception {
        fixtures.preparar(ApuracaoFixtures.AUSENTE);
        assertEquals(400, enviar("GET", "/apuracoes/F404/2026-13").statusCode());
        assertEquals(400, enviar("POST", "/apuracoes/F404/2026-1/calcular").statusCode());
        assertEquals(400, enviar("GET", "/apuracoes/123/2026-10").statusCode());
        assertEquals(404, enviar("POST", "/apuracoes/F999/2026-10/calcular").statusCode());
        assertEquals(0, fixtures.apuracoes());
    }

    @Test
    void serializaPrimeiroCalculoConcorrente() throws Exception {
        fixtures.prepararPontos("SIND-TI", 456);
        var request = requisicao("POST", RESUMO + "/calcular");
        var primeira = http.sendAsync(request, HttpResponse.BodyHandlers.ofString());
        var segunda = http.sendAsync(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(corpo(primeira.get()), corpo(segunda.get()));
        assertEquals(1, fixtures.apuracoes());
    }

    private void assertHoras(String esperado, JsonNode json, String campo) {
        var numero = json.get(campo);
        assertTrue(numero != null && numero.isNumber(), campo + " deve ser numérico");
        assertEquals(new BigDecimal(esperado).stripTrailingZeros(), numero.decimalValue().stripTrailingZeros(),
                campo + " em horas");
    }

    private JsonNode corpo(HttpResponse<String> resposta) throws IOException {
        assertEquals(200, resposta.statusCode(), resposta.body());
        return mapper.readTree(resposta.body());
    }

    private HttpResponse<String> enviar(String metodo, String caminho) throws IOException, InterruptedException {
        return http.send(requisicao(metodo, caminho), HttpResponse.BodyHandlers.ofString());
    }

    private HttpRequest requisicao(String metodo, String caminho) {
        return HttpRequest.newBuilder(URI.create(baseUrl.toExternalForm()).resolve(caminho)).timeout(Duration.ofSeconds(10))
                .header("Accept", "application/json")
                .method(metodo, HttpRequest.BodyPublishers.noBody()).build();
    }
}
