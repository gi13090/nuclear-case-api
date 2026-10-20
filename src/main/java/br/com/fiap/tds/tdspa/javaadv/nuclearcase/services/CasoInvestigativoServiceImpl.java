package br.com.fiap.tds.tdspa.javaadv.nuclearcase.services;

import br.com.fiap.tds.tdspa.javaadv.nuclearcase.datasource.repositories.CasoInvestigativoRepository;
import br.com.fiap.tds.tdspa.javaadv.nuclearcase.domainmodel.entities.CasoInvestigativo;
import br.com.fiap.tds.tdspa.javaadv.nuclearcase.infrastructure.exceptions.RecursoNaoEncontradoException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CasoInvestigativoServiceImpl implements CasoInvestigativoService {
    private final CasoInvestigativoRepository repository;

    @Override
    public List<CasoInvestigativo> listar() { return repository.findAll(); }

    @Override
    public CasoInvestigativo buscarPorId(UUID id) {
        return repository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException(id));
    }

    @Override
    public List<CasoInvestigativo> buscarPorTitulo(String titulo) {
        return repository.findByTituloContainingIgnoreCase(titulo.trim());
    }

    @Override
    @Transactional
    public CasoInvestigativo criar(CasoInvestigativo caso) { return repository.save(caso); }

    @Override
    @Transactional
    public CasoInvestigativo atualizar(UUID id, CasoInvestigativo dados) {
        CasoInvestigativo existente = buscarPorId(id);
        existente.setTitulo(dados.getTitulo());
        existente.setDescricao(dados.getDescricao());
        existente.setCategoria(dados.getCategoria());
        existente.setDificuldade(dados.getDificuldade());
        existente.setExplicacaoCientifica(dados.getExplicacaoCientifica());
        return existente;
    }

    @Override
    @Transactional
    public void excluir(UUID id) { repository.delete(buscarPorId(id)); }
}
