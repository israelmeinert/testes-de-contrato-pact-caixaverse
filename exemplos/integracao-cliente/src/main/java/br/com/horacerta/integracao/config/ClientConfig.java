package br.com.horacerta.integracao.config;

import java.net.URI;
import java.net.http.HttpClient;
import java.time.Duration;

import br.com.horacerta.integracao.clients.ApuracaoClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class ClientConfig {
    @Produces
    @ApplicationScoped
    ApuracaoClient apuracao(@ConfigProperty(name = "apuracao.url") URI url) {
        var http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();
        return new ApuracaoClient(http, url, JsonConfig.mapper());
    }
}
