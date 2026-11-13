package br.com.horacerta.apuracao.entities;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import org.hibernate.annotations.Check;

@Entity
@Check(constraints = "jornadaDiariaMinutos > 0")
public class Sindicato extends PanacheEntity {
    @Column(nullable = false, unique = true)
    public String codigo;

    public int jornadaDiariaMinutos;
}
