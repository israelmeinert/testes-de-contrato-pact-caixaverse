package br.com.horacerta.ferias.clients;

public class IntegracaoException extends RuntimeException {
    public IntegracaoException(int status) {
        super("Status inesperado da apuração: " + status);
    }

    public IntegracaoException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
