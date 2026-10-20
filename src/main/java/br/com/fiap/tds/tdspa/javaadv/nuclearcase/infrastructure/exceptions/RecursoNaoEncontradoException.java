package br.com.fiap.tds.tdspa.javaadv.nuclearcase.infrastructure.exceptions;

import java.util.UUID;

public class RecursoNaoEncontradoException extends RuntimeException {
    public RecursoNaoEncontradoException(UUID id) {
        super("Caso investigativo não encontrado: " + id);
    }
}
