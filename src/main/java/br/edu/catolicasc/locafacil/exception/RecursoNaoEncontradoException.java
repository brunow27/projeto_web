package br.edu.catolicasc.locafacil.exception;

/** Recurso inexistente (HTTP 404). */
public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String recurso, Object id) {
        super(recurso + " não encontrado(a) (id=" + id + ")");
    }
}
