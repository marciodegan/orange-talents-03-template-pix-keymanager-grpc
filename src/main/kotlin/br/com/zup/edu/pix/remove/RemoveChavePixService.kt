package br.com.zup.edu.pix.remove

import br.com.zup.edu.bcb.BcbClient
import br.com.zup.edu.bcb.BcbPixDeleteRequest
import br.com.zup.edu.compartilhado.ValidUUID
import br.com.zup.edu.exception.ChavePixNaoEncontradaException
import br.com.zup.edu.pix.ChavePixRepository
import io.micronaut.http.HttpStatus
import io.micronaut.validation.Validated
import org.slf4j.LoggerFactory
import java.lang.IllegalStateException
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.constraints.NotBlank

@Validated
@Singleton
class RemoveChavePixService(
    @Inject val repository: ChavePixRepository,
    @Inject val bcbClient: BcbClient
    ) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @Transactional
    fun deleta(
        @NotBlank @ValidUUID(message = "Cliente ID em formato inválido") clienteId: String?,
        @NotBlank @ValidUUID(message = "Chave Pix em formato inválido") pixId: String?
    ) {
        val pixId = UUID.fromString(pixId)
        val clienteId = UUID.fromString(clienteId)

        val chave = repository.findByIdAndClienteId(pixId, clienteId)
            .orElseThrow{ ChavePixNaoEncontradaException("Chave pix ou clienteID não encontrado(s)")}

        repository.deleteById(pixId)
        logger.info("Chave deletada no bd => ${chave.chave}")

        val request = BcbPixDeleteRequest(chave.chave)

        val bcbResponse = bcbClient.deleta(key = chave.chave, request = request)
        logger.info("Chave deletada no BCB => ${bcbResponse.body()} | Status: ${bcbResponse.status.code}")

        if(bcbResponse.status != HttpStatus.OK){
            throw IllegalStateException("Erro ao remover chave Pix no BCB")
        }
    }
}