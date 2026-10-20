package br.com.fiap.tds.tdspa.javaadv.nuclearcase.domainmodel.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "casos_investigativos")
@Getter
@Setter
@NoArgsConstructor
public class CasoInvestigativo {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Setter(AccessLevel.NONE)
    private UUID id;

    @Column(nullable = false, length = 150)
    private String titulo;

    @Column(nullable = false, length = 1000)
    private String descricao;

    @Column(nullable = false, length = 50)
    private String categoria;

    @Column(nullable = false)
    private Integer dificuldade;

    @Column(name = "explicacao_cientifica", nullable = false, length = 2000)
    private String explicacaoCientifica;
}
