package br.com.horacerta.integracao;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Consumer;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.net.httpserver.HttpServer;
import br.com.horacerta.integracao.clients.ApuracaoClient;
import br.com.horacerta.integracao.clients.IntegracaoException;
import br.com.horacerta.integracao.config.JsonConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApuracaoClientRobustezTest {
    private static final String RESPOSTA = """
            {"funcionarioId":"F001","competencia":"2026-10","horasTrabalhadas":160.00,"horasExtras":8.00}
            """;
    private HttpServer servidor;

    @AfterEach
    void pararServidor() {
        if (servidor != null) {
            servidor.stop(0);
        }
    }

    @Test
    void aceitaCamposExtras() throws IOException {
        var json = resposta();
        json.putObject("sindicato").put("codigo", "SIND-TI");
        json.put("campoNovo", true);
        var resumo = client(200, json.toString()).consultar("F001", "2026-10");
        assertEquals(0, new BigDecimal("160").compareTo(resumo.horasTrabalhadas()));
    }

    @Test
    void aceitaZeroExplicito() throws IOException {
        var json = resposta();
        json.put("horasTrabalhadas", 0);
        var resumo = client(200, json.toString()).consultar("F001", "2026-10");
        assertEquals(0, new BigDecimal("0").compareTo(resumo.horasTrabalhadas()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"\"inválido\"", "\"160\"", "true", "{}"})
    void rejeitaTipoIncompativel(String literal) throws IOException {
        var json = resposta();
        json.set("horasTrabalhadas", JsonConfig.mapper().readTree(literal));
        assertThrows(IntegracaoException.class, () -> client(200, json.toString()).consultar("F001", "2026-10"));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 3, 4, 5})
    void rejeitaCampoObrigatorioInvalido(int caso) throws IOException {
        List<Consumer<ObjectNode>> alteracoes = List.of(
            (ObjectNode json) -> { json.remove("horasTrabalhadas"); },
            (ObjectNode json) -> { json.putNull("horasTrabalhadas"); },
            (ObjectNode json) -> { json.put("funcionarioId", " "); },
            (ObjectNode json) -> { json.put("competencia", 202610); },
            (ObjectNode json) -> { json.remove("funcionarioId"); },
            (ObjectNode json) -> { json.remove("horasExtras"); });
        var json = resposta();
        alteracoes.get(caso).accept(json);
        assertThrows(IntegracaoException.class, () -> client(200, json.toString()).consultar("F001", "2026-10"));
    }

    @Test
    void diferenciaStatusInesperado() throws IOException {
        var cliente = client(503, "indisponível");
        var erro = assertThrows(IntegracaoException.class, () -> cliente.consultar("F001", "2026-10"));
        assertTrue(erro.getMessage().contains("503"));
    }

    private ObjectNode resposta() throws IOException {
        return (ObjectNode) JsonConfig.mapper().readTree(RESPOSTA);
    }

    private ApuracaoClient client(int status, String corpo) throws IOException {
        servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        servidor.createContext("/apuracoes/F001/2026-10", troca -> {
            byte[] bytes = corpo.getBytes(StandardCharsets.UTF_8);
            try (troca) {
                troca.getResponseHeaders().set("Content-Type", "application/json");
                troca.sendResponseHeaders(status, bytes.length);
                troca.getResponseBody().write(bytes);
            }
        });
        servidor.start();
        var uri = URI.create("http://127.0.0.1:" + servidor.getAddress().getPort());
        return new ApuracaoClient(HttpClient.newHttpClient(), uri, JsonConfig.mapper());
    }
}
