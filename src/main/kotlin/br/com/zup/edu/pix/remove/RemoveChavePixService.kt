package br.com.zup.edu.pix.remove

import br.com.zup.edu.compartilhado.ValidUUID
import br.com.zup.edu.exception.ChavePixNaoEncontradaException
import br.com.zup.edu.pix.ChavePixRepository
import io.micronaut.validation.Validated
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.constraints.NotBlank

@Validated
@Singleton
class RemoveChavePixService(@Inject val repository: ChavePixRepository) {

    @Transactional
    fun deleta(
        @NotBlank @ValidUUID(message = "Id do cliente em formato inválido") clienteId: String?,
        @NotBlank @ValidUUID(message = "Chave Pix em formato inválido") pixId: String?
    ) {
        val pixId = UUID.fromString(pixId)
        val clienteId = UUID.fromString(clienteId)

        if (!repository.existsByIdAndClienteId(pixId, clienteId)) {
            throw (ChavePixNaoEncontradaException("Chave pix ou clienteID não encontrados"))
        }
        repository.deleteById(pixId)
    }
}