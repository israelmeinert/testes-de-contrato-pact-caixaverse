package br.com.horacerta.apuracao.dtos;

import java.math.BigDecimal;

public record ApuracaoResponse(
        String funcionarioId,
        String competencia,
        BigDecimal horasTrabalhadas,
        BigDecimal horasExtras,
        int diasTrabalhados,
        SindicatoResponse sindicato) {}
