package br.com.horacerta.apuracao.entities;

import java.time.LocalDate;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.Check;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"funcionarioId", "data"}))
@Check(constraints = "minutosTrabalhados >= 0")
public class RegistroPonto extends PanacheEntity {
    @Column(nullable = false)
    public String funcionarioId;

    @Column(nullable = false)
    public LocalDate data;

    public int minutosTrabalhados;
}
