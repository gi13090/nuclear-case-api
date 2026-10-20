package br.com.fiap.tds.tdspa.javaadv.nuclearcase.services;

import br.com.fiap.tds.tdspa.javaadv.nuclearcase.domainmodel.entities.CasoInvestigativo;
import java.util.List;
import java.util.UUID;

public interface CasoInvestigativoService {
    List<CasoInvestigativo> listar();
    CasoInvestigativo buscarPorId(UUID id);
    List<CasoInvestigativo> buscarPorTitulo(String titulo);
    CasoInvestigativo criar(CasoInvestigativo caso);
    CasoInvestigativo atualizar(UUID id, CasoInvestigativo dados);
    void excluir(UUID id);
}
