package br.com.horacerta.apuracao.entities;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"funcionarioId", "competencia"}))
public class ApuracaoMensal extends PanacheEntity {
    @Column(nullable = false)
    public String funcionarioId;

    @Column(nullable = false, length = 7)
    public String competencia;

    public long minutosTrabalhados;
    public long minutosExtras;
    public int diasTrabalhados;
    public String sindicatoCodigo;

    public static ApuracaoMensal buscar(String id, String competencia) {
        return find("funcionarioId = ?1 and competencia = ?2", id, competencia).firstResult();
    }
}
