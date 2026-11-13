package br.com.horacerta.apuracao;

import java.math.BigDecimal;
import java.util.List;

import br.com.horacerta.apuracao.services.CalculoMensal;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CalculoMensalCalculoTest {
    @Test
    void somaExtrasPorDiaSemCompensarDiaCurto() {
        var totais = CalculoMensal.somarDias(List.of(480, 400, 456, 0), 456);
        assertEquals(new CalculoMensal.Totais(1336, 24, 3), totais);
    }

    @Test
    void converteComDuasCasasEArredonda() {
        assertEquals(new BigDecimal("0.02"), CalculoMensal.horas(1));
        assertEquals(new BigDecimal("0.13"), CalculoMensal.horas(8));
        assertEquals(new BigDecimal("160.00"), CalculoMensal.horas(9600));
    }
}
