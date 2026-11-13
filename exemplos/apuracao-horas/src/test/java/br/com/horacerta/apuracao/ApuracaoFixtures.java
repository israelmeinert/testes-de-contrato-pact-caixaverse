package br.com.horacerta.apuracao;

import java.time.LocalDate;

import br.com.horacerta.apuracao.entities.ApuracaoMensal;
import br.com.horacerta.apuracao.entities.Funcionario;
import br.com.horacerta.apuracao.entities.RegistroPonto;
import br.com.horacerta.apuracao.entities.Sindicato;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class ApuracaoFixtures {
    static final String APURADO = "F001 apurado em 2026-10";
    static final String AUSENTE = "F404 sem apuracao em 2026-10";

    @Transactional
    public void preparar(String estado) {
        limpar();
        if (APURADO.equals(estado)) {
            funcionario("F001", sindicato("SIND-TI", 456));
            var apuracao = new ApuracaoMensal();
            apuracao.funcionarioId = "F001";
            apuracao.competencia = "2026-10";
            apuracao.minutosTrabalhados = 9600;
            apuracao.minutosExtras = 480;
            apuracao.diasTrabalhados = 20;
            apuracao.sindicatoCodigo = "SIND-TI";
            apuracao.persist();
        } else if (AUSENTE.equals(estado)) {
            funcionario("F404", null);
        } else {
            throw new IllegalArgumentException(estado);
        }
    }

    @Transactional
    public void prepararPontos(String codigoSindicato, int jornada) {
        limpar();
        funcionario("F001", codigoSindicato == null ? null : sindicato(codigoSindicato, jornada));
        funcionario("F404", null);
        int[] dias = {1, 2, 5, 6, 7, 8, 9, 13, 14, 15, 16, 19, 20, 21, 22, 23, 26, 27, 29, 30};
        for (int dia : dias) {
            ponto(LocalDate.of(2026, 10, dia), 480);
        }
        ponto(LocalDate.of(2026, 10, 31), 0);
        ponto(LocalDate.of(2026, 9, 30), 480);
        ponto(LocalDate.of(2026, 11, 1), 480);
    }

    @Transactional
    public long apuracoes() {
        return ApuracaoMensal.count();
    }

    private static void limpar() {
        ApuracaoMensal.deleteAll();
        RegistroPonto.deleteAll();
        Funcionario.deleteAll();
        Sindicato.deleteAll();
    }

    private static void ponto(LocalDate data, int minutos) {
        var ponto = new RegistroPonto();
        ponto.funcionarioId = "F001";
        ponto.data = data;
        ponto.minutosTrabalhados = minutos;
        ponto.persist();
    }

    private static void funcionario(String codigo, Sindicato sindicato) {
        var funcionario = new Funcionario();
        funcionario.codigo = codigo;
        funcionario.sindicato = sindicato;
        funcionario.persist();
    }

    private static Sindicato sindicato(String codigo, int jornada) {
        var sindicato = new Sindicato();
        sindicato.codigo = codigo;
        sindicato.jornadaDiariaMinutos = jornada;
        sindicato.persist();
        return sindicato;
    }
}
