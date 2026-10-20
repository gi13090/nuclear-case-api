package br.com.fiap.tds.tdspa.javaadv.nuclearcase.infrastructure.runner;

import br.com.fiap.tds.tdspa.javaadv.nuclearcase.datasource.repositories.CasoInvestigativoRepository;
import br.com.fiap.tds.tdspa.javaadv.nuclearcase.domainmodel.entities.CasoInvestigativo;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {
    private final CasoInvestigativoRepository repository;

    @Override
    @Transactional
    public void run(String... args) {
        if (repository.count() != 0) return;
        repository.saveAll(List.of(
                caso("O alimento irradiado", "Investigue a diferença entre irradiação e contaminação radioativa.",
                        "ALIMENTACAO", 1, "A irradiação de alimentos é um processo controlado. Irradiação e contaminação radioativa são conceitos distintos; condições e normas do processo precisam ser consideradas."),
                caso("O segredo do diagnóstico", "Explore como radiofármacos podem apoiar exames médicos.",
                        "SAUDE", 2, "Alguns exames de medicina nuclear utilizam radiofármacos para obter informações sobre o funcionamento de órgãos. A indicação e a avaliação de riscos e benefícios cabem a profissionais de saúde."),
                caso("O futuro energético", "Compare benefícios e desafios da geração nuclear de eletricidade.",
                        "ENERGIA", 3, "A geração nuclear utiliza energia liberada em reações nucleares. Sua avaliação envolve segurança, gestão de resíduos, custos e impactos ambientais ao longo do ciclo de vida.")
        ));
    }

    private CasoInvestigativo caso(String titulo, String descricao, String categoria, int dificuldade, String explicacao) {
        var caso = new CasoInvestigativo();
        caso.setTitulo(titulo);
        caso.setDescricao(descricao);
        caso.setCategoria(categoria);
        caso.setDificuldade(dificuldade);
        caso.setExplicacaoCientifica(explicacao);
        return caso;
    }
}
