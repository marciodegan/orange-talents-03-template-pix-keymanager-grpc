package br.com.zup.edu.pix.registra

import br.com.zup.edu.ContasDeClientesNoItauClient
import br.com.zup.edu.exception.ChavePixExistenceException
import io.micronaut.validation.Validated
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.Valid

@Validated
@Singleton
class NovaChavePixService(@Inject val repository: ChavePixRepository,
                          @Inject val itauClient: ContasDeClientesNoItauClient
){

    private val logger = LoggerFactory.getLogger(this::class.java)

    @Transactional
    fun registra(@Valid novaChave: NovaChavePix) : ChavePix {

        // 1. Verifica se chave já existe no sistema
        if(repository.existsByChave(novaChave.chave))
            throw ChavePixExistenceException("Chave Pix ${novaChave.chave} já existe")

        // 2. Busca dados da conta no ERP do Itau
        val response = itauClient.buscaContaPorTipo(novaChave.clienteId!!, novaChave.tipoDeConta!!.name) // submete p/ o itauClient c/ id e tipo
        val conta = response.body()?.toModel() ?: throw IllegalStateException("Cliente não encontrado no Itau") // devolve 404 c/ body vazio caso não encontrado

        // 3. grava no banco de dados
        val chave = novaChave.toModel(conta)
        repository.save(chave)

        logger.info("Chave criada com sucesso: ${chave.chave} - id: ${chave.id}")
        return chave
    }
}