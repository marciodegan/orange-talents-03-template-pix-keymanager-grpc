package br.com.zup.edu.exception

import io.grpc.Status
import javax.inject.Singleton

@Singleton
class ChavePixNaoEncontradaExceptionHandler
    : ExceptionHandler<ChavePixNaoEncontradaException> {

    override fun handle(e: ChavePixNaoEncontradaException): ExceptionHandler.StatusWithDetails {
        return ExceptionHandler.StatusWithDetails(Status.NOT_FOUND
            .withDescription(e.message)
            .withCause(e))
    }

    // verifica se a exceção lancada pelo endpoint é do tipo ChavePixNaoEncontrada
    override fun supports(e: Exception): Boolean {
        return e is ChavePixNaoEncontradaException
    }
}