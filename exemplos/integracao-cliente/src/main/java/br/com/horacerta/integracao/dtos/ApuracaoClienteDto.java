package br.com.horacerta.integracao.dtos;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ApuracaoClienteDto(
        String funcionarioId,
        String competencia,
        BigDecimal horasTrabalhadas,
        BigDecimal horasExtras) {
    public ApuracaoClienteDto {
        if (funcionarioId == null || funcionarioId.isBlank() || competencia == null || competencia.isBlank()
                || horasTrabalhadas == null || horasExtras == null) {
            throw new IllegalArgumentException("Campos obrigatórios da apuração");
        }
    }
}
