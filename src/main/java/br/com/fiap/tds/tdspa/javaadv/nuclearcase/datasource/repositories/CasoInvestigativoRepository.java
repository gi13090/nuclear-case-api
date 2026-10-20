package br.com.fiap.tds.tdspa.javaadv.nuclearcase.datasource.repositories;

import br.com.fiap.tds.tdspa.javaadv.nuclearcase.domainmodel.entities.CasoInvestigativo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface CasoInvestigativoRepository extends JpaRepository<CasoInvestigativo, UUID> {
    List<CasoInvestigativo> findByTituloContainingIgnoreCase(String titulo);
}
