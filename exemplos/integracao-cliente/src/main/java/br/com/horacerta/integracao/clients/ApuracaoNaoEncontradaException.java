package br.com.horacerta.integracao.clients;

public class ApuracaoNaoEncontradaException extends RuntimeException {
    public ApuracaoNaoEncontradaException(String id, String mes) {
        super("Apuração ausente para " + id + "/" + mes);
    }
}
