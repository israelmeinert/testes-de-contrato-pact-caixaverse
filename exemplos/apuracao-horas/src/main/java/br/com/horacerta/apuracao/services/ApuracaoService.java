package br.com.horacerta.apuracao.services;

import java.time.YearMonth;

import br.com.horacerta.apuracao.entities.ApuracaoMensal;
import br.com.horacerta.apuracao.entities.Funcionario;
import br.com.horacerta.apuracao.entities.RegistroPonto;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;

@ApplicationScoped
public class ApuracaoService {
    @Transactional
    public ApuracaoMensal consultar(String id, YearMonth mes) {
        var apuracao = ApuracaoMensal.buscar(id, mes.toString());
        if (apuracao == null) {
            throw new NotFoundException();
        }
        return apuracao;
    }

    @Transactional
    public ApuracaoMensal calcular(String id, YearMonth mes) {
        // Serializa fechamentos do mesmo funcionário, inclusive o primeiro insert.
        var funcionario = Funcionario.<Funcionario>find("codigo", id)
                .withLock(LockModeType.PESSIMISTIC_WRITE).firstResult();
        if (funcionario == null) {
            throw new NotFoundException();
        }
        var pontos = RegistroPonto.<RegistroPonto>list("funcionarioId = ?1 and data >= ?2 and data <= ?3",
                id, mes.atDay(1), mes.atEndOfMonth());
        var sindicato = funcionario.sindicato;
        int jornada = sindicato == null ? CalculoMensal.JORNADA_PADRAO : sindicato.jornadaDiariaMinutos;
        var totais = CalculoMensal.somarDias(pontos.stream().map(ponto -> ponto.minutosTrabalhados).toList(), jornada);

        var apuracao = ApuracaoMensal.buscar(id, mes.toString());
        if (apuracao == null) {
            apuracao = new ApuracaoMensal();
            apuracao.funcionarioId = id;
            apuracao.competencia = mes.toString();
        }
        apuracao.minutosTrabalhados = totais.minutosTrabalhados();
        apuracao.minutosExtras = totais.minutosExtras();
        apuracao.diasTrabalhados = totais.diasTrabalhados();
        apuracao.sindicatoCodigo = sindicato == null ? null : sindicato.codigo;
        apuracao.persist();
        return apuracao;
    }
}
