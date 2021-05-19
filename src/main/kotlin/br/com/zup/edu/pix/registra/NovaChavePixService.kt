package br.com.zup.edu.pix.registra

import br.com.zup.edu.ContasDeClientesNoItauClient
import br.com.zup.edu.bcb.BcbClient
import br.com.zup.edu.bcb.BcbPixRequest
import br.com.zup.edu.exception.ChavePixExistenceException
import br.com.zup.edu.pix.ChavePix
import br.com.zup.edu.pix.ChavePixRepository
import io.micronaut.http.HttpStatus
import io.micronaut.validation.Validated
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.Valid

@Validated
@Singleton
class NovaChavePixService(@Inject val repository: ChavePixRepository,
                          @Inject val itauClient: ContasDeClientesNoItauClient,
                          @Inject val bcbClient: BcbClient
){

    private val logger = LoggerFactory.getLogger(this::class.java)

    @Transactional
    fun registra(@Valid novaChave: NovaChavePix) : ChavePix {

        // 1. Verifica se chave já existe no sistema
        if(repository.existsByChave(novaChave.chave))
            throw ChavePixExistenceException("Chave Pix ${novaChave.chave} já existe")

        // 2. Busca dados da conta no ERP do Itau
        val response = itauClient.buscaContaPorTipo(novaChave.clienteId!!, novaChave.tipoDeConta!!.name)
        val conta = response.body()?.toModel() ?: throw IllegalStateException("Cliente não encontrado no Itau")

        // 3. grava no banco de dados
        val chave = novaChave.toModel(conta).also{
            logger.info("primeira chave gravada no bd => ${it.chave}")
        }
        repository.save(chave)

        // 4. Registra chave no bcb
        val bcbRequest = BcbPixRequest.toClient(chave).also {
            logger.info("Registrando chave no BCB => ${it.key} | ${it.keyType} | ${it.owner}")
        }

        val bcbResponse = bcbClient.registra(bcbRequest).also {
            logger.info("Chave retornada pelo bcb => ${it.body().key} | ${it.body().keyType} | ${it.body().owner}")
        }

        if(bcbResponse.status != HttpStatus.CREATED)
            throw java.lang.IllegalStateException("Erro ao registrar chave Pix no BCB")

        // 5. atualiza chave do dominio com chave aleatoria gerada pelo BCB (somente p/ chaves aleatorias)
        chave.atualiza(bcbResponse.body()!!.key).also{
            logger.info("Chave registrada com sucesso => ${chave.chave} - id: ${chave.id}")
        }
        return chave
    }
}