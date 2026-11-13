package br.com.horacerta.apuracao.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public final class CalculoMensal {
    public static final int JORNADA_PADRAO = 456;

    private CalculoMensal() {}

    // A consulta devolve um registro por data, conforme a restrição do banco.
    public static Totais somarDias(List<Integer> minutosPorDia, int jornada) {
        long total = 0;
        long extras = 0;
        int dias = 0;
        for (int minutos : minutosPorDia) {
            total += minutos;
            extras += Math.max(0, minutos - jornada);
            if (minutos > 0) {
                dias++;
            }
        }
        return new Totais(total, extras, dias);
    }

    public static BigDecimal horas(long minutos) {
        return BigDecimal.valueOf(minutos).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
    }

    public record Totais(long minutosTrabalhados, long minutosExtras, int diasTrabalhados) {}
}
