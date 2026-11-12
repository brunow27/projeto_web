package br.edu.catolicasc.locafacil.exception;

/** Conflito com o estado atual: duplicidade, dependências, agenda ocupada (HTTP 409). */
public class ConflitoException extends RuntimeException {

    public ConflitoException(String mensagem) {
        super(mensagem);
    }
}
