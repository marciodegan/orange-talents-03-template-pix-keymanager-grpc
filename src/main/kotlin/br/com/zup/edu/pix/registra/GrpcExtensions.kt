package br.com.zup.edu

import br.com.zup.edu.pix.TipoDeChaveEnum
import br.com.zup.edu.pix.TipoDeContaEnum
import br.com.zup.edu.pix.registra.NovaChavePix

// extension function que converte p/ dto NovaChavePix
fun RegistraChavePixRequest.toModel() : NovaChavePix {
    val UNKNOWN_TIPO_CHAVE = null
    val UNKNOWN_TIPO_CONTA = null

    return NovaChavePix(
        clienteId = clienteId,
        tipo = when (tipoDeChave){
            UNKNOWN_TIPO_CHAVE -> null
            else -> TipoDeChaveEnum.valueOf(tipoDeChave.name)
        },
        chave = chave,
        tipoDeConta = when (tipoDeConta){
            UNKNOWN_TIPO_CONTA -> null
            else -> TipoDeContaEnum.valueOf(tipoDeConta.name)
        }
    )
}