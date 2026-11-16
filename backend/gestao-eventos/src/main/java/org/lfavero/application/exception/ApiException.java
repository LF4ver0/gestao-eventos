package org.lfavero.application.exception;

public class ApiException extends RuntimeException {

    private final int status;
    private final String campo;

    protected ApiException(int status, String mensagem, String campo) {
        super(mensagem);
        this.status = status;
        this.campo = campo;
    }

    public int getStatus() {
        return status;
    }

    public String getCampo() {
        return campo;
    }

    public static ApiException naoEncontrado(String mensagem) {
        return new ApiException(404, mensagem, null);
    }

    public static ApiException conflito(String mensagem) {
        return new ApiException(409, mensagem, null);
    }

    public static ApiException validacao(String campo, String mensagem) {
        return new ApiException(400, mensagem, campo);
    }
}