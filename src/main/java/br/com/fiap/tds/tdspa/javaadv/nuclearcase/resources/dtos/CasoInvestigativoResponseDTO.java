package br.com.fiap.tds.tdspa.javaadv.nuclearcase.resources.dtos;

import br.com.fiap.tds.tdspa.javaadv.nuclearcase.domainmodel.entities.CasoInvestigativo;
import java.util.UUID;

public record CasoInvestigativoResponseDTO(UUID id, String titulo, String descricao,
        String categoria, Integer dificuldade, String explicacaoCientifica) {
    public static CasoInvestigativoResponseDTO fromEntity(CasoInvestigativo caso) {
        return new CasoInvestigativoResponseDTO(caso.getId(), caso.getTitulo(), caso.getDescricao(),
                caso.getCategoria(), caso.getDificuldade(), caso.getExplicacaoCientifica());
    }
}
