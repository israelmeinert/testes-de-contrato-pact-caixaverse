package br.com.horacerta.apuracao.config;

import java.time.LocalDate;

import br.com.horacerta.apuracao.entities.Funcionario;
import br.com.horacerta.apuracao.entities.RegistroPonto;
import br.com.horacerta.apuracao.entities.Sindicato;
import io.quarkus.arc.profile.IfBuildProfile;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.transaction.Transactional;

@ApplicationScoped
@IfBuildProfile("demo")
public class MassaDemonstracao {
    @Transactional
    void cadastrar(@Observes StartupEvent evento) {
        var ti = cadastrarSindicato("SIND-TI", 456);
        var log = cadastrarSindicato("SIND-LOG", 420);
        cadastrarFuncionario("F001", ti, true);
        cadastrarFuncionario("F002", log, true);
        cadastrarFuncionario("F003", null, true);
        cadastrarFuncionario("F404", null, false);
    }

    private static Sindicato cadastrarSindicato(String codigo, int jornada) {
        var sindicato = new Sindicato();
        sindicato.codigo = codigo;
        sindicato.jornadaDiariaMinutos = jornada;
        sindicato.persist();
        return sindicato;
    }

    private static void cadastrarFuncionario(String codigo, Sindicato sindicato, boolean comPontos) {
        var funcionario = new Funcionario();
        funcionario.codigo = codigo;
        funcionario.sindicato = sindicato;
        funcionario.persist();
        if (comPontos) {
            int[] dias = {1, 2, 5, 6, 7, 8, 9, 13, 14, 15, 16, 19, 20, 21, 22, 23, 26, 27, 29, 30};
            for (int dia : dias) {
                var ponto = new RegistroPonto();
                ponto.funcionarioId = codigo;
                ponto.data = LocalDate.of(2026, 10, dia);
                ponto.minutosTrabalhados = 480;
                ponto.persist();
            }
        }
    }
}
