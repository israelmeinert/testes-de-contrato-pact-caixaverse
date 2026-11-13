package br.com.horacerta.integracao;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.util.Map;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTest;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.PactSpecVersion;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import br.com.horacerta.integracao.clients.ApuracaoClient;
import br.com.horacerta.integracao.clients.ApuracaoNaoEncontradaException;
import br.com.horacerta.integracao.config.JsonConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@PactConsumerTest
@PactTestFor(providerName = "apuracao-horas", pactVersion = PactSpecVersion.V3)
class IntegracaoClientePactTest {
    @Pact(consumer = "integracao-cliente")
    public RequestResponsePact mensal(PactDslWithProvider builder) {
        var body = new PactDslJsonBody()
                .stringValue("funcionarioId", "F001")
                .stringValue("competencia", "2026-10")
                .numberType("horasTrabalhadas", 160.0)
                .numberType("horasExtras", 8.0);
        return builder.given("F001 apurado em 2026-10")
                .uponReceiving("consulta mensal de integracao-cliente")
                .method("GET").path("/apuracoes/F001/2026-10")
                .headers(Map.of("Accept", "application/json"))
                .willRespondWith().status(200)
                .headers(Map.of("Content-Type", "application/json"))
                .body(body).toPact();
    }

    @Pact(consumer = "integracao-cliente")
    public RequestResponsePact ausente(PactDslWithProvider builder) {
        return builder.given("F404 sem apuracao em 2026-10")
                .uponReceiving("consulta de apuracao ausente")
                .method("GET").path("/apuracoes/F404/2026-10")
                .headers(Map.of("Accept", "application/json"))
                .willRespondWith().status(404)
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "mensal")
    void leResumoDoMes(MockServer server) {
        var resultado = client(server).consultar("F001", "2026-10");
        assertEquals("F001", resultado.funcionarioId());
        assertEquals("2026-10", resultado.competencia());
        assertEquals(0, new BigDecimal("160").compareTo(resultado.horasTrabalhadas()));
        assertEquals(0, new BigDecimal("8").compareTo(resultado.horasExtras()));
    }

    @Test
    @PactTestFor(pactMethod = "ausente")
    void mapeia404SemDependerDeCorpo(MockServer server) {
        assertThrows(ApuracaoNaoEncontradaException.class,
                () -> client(server).consultar("F404", "2026-10"));
    }

    private ApuracaoClient client(MockServer server) {
        return new ApuracaoClient(HttpClient.newHttpClient(),
                URI.create(server.getUrl()), JsonConfig.mapper());
    }
}
