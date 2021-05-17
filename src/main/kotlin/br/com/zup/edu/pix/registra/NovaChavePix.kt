package br.com.zup.edu.pix.registra

import br.com.zup.edu.ValidPixKey
import br.com.zup.edu.compartilhado.ValidUUID
import br.com.zup.edu.pix.ChavePix
import br.com.zup.edu.pix.ContaAssociada
import br.com.zup.edu.pix.TipoDeChaveEnum
import br.com.zup.edu.pix.TipoDeContaEnum
import io.micronaut.core.annotation.Introspected
import java.util.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@ValidPixKey
@Introspected
data class NovaChavePix(

    @ValidUUID
    @field:NotBlank
    val clienteId: String?,

    @field:NotNull
    val tipo: TipoDeChaveEnum?,

    @field:Size(max=77)
    val chave: String?,

    @field:NotNull
    val tipoDeConta: TipoDeContaEnum?
){

    // converte o tipo NovaChave para o tipo ChavePix
    fun toModel(conta: ContaAssociada): ChavePix {
        return ChavePix(
            clienteId = UUID.fromString(this.clienteId),
            tipo = TipoDeChaveEnum.valueOf(this.tipo!!.name), // converte o tipo que veio para o tipo de chave da entidade
            chave = if(this.tipo == TipoDeChaveEnum.ALEATORIA) UUID.randomUUID().toString() else this.chave!!, // se o tipo de chave for aleatória, o valor da chave deve ser gerado pelo sistema. Se for fone, cpf, email...usa o chave informada pelo usuário
            tipoDeConta = TipoDeContaEnum.valueOf(this.tipoDeConta!!.name),
            conta = conta // conta associada passada como parâmetro no método
        )
    }
}
