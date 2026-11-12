package br.edu.catolicasc.locafacil.exception;

/** Requisição que viola uma regra de negócio (HTTP 400). */
public class RegraNegocioException extends RuntimeException {

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
