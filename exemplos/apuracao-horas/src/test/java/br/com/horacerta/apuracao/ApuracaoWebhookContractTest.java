package br.com.horacerta.apuracao;

import java.net.URL;

import au.com.dius.pact.provider.junit5.HttpTestTarget;
import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.loader.PactBroker;
import au.com.dius.pact.provider.junitsupport.AllowOverridePactUrl;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

@QuarkusTest
@Provider("apuracao-horas")
@PactBroker
@AllowOverridePactUrl
public class ApuracaoWebhookContractTest {
    @TestHTTPResource URL baseUrl;
    @Inject ApuracaoFixtures fixtures;

    @BeforeEach
    void configurarAlvo(PactVerificationContext context) {
        context.setTarget(HttpTestTarget.fromUrl(baseUrl));
    }

    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider.class)
    void verificar(PactVerificationContext context) {
        context.verifyInteraction();
    }

    @State(ApuracaoFixtures.APURADO)
    public void apurado() {
        fixtures.preparar(ApuracaoFixtures.APURADO);
    }

    @State(ApuracaoFixtures.AUSENTE)
    public void ausente() {
        fixtures.preparar(ApuracaoFixtures.AUSENTE);
    }
}
