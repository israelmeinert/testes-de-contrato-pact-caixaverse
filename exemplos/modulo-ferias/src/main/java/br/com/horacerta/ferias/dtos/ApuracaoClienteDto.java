package br.com.horacerta.ferias.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ApuracaoClienteDto(
        String funcionarioId,
        String competencia,
        Integer diasTrabalhados) {
    public ApuracaoClienteDto {
        if (funcionarioId == null || funcionarioId.isBlank() || competencia == null || competencia.isBlank()
                || diasTrabalhados == null) {
            throw new IllegalArgumentException("Campos obrigatórios da apuração");
        }
    }
}
