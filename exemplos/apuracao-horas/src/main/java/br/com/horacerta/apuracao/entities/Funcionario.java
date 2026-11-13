package br.com.horacerta.apuracao.entities;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;

@Entity
public class Funcionario extends PanacheEntity {
    @Column(nullable = false, unique = true)
    public String codigo;

    @ManyToOne
    public Sindicato sindicato;
}
