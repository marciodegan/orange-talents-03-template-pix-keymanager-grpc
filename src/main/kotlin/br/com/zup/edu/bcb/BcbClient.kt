package br.com.zup.edu.bcb

import br.com.zup.edu.pix.ChavePix
import br.com.zup.edu.pix.ContaAssociada
import br.com.zup.edu.pix.TipoDeChaveEnum
import br.com.zup.edu.pix.TipoDeContaEnum
import br.com.zup.edu.pix.carrega.ChavePixInfo
import br.com.zup.edu.pix.carrega.Instituicoes
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
import io.micronaut.http.client.annotation.Client
import java.lang.IllegalArgumentException
import java.time.LocalDateTime
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Client(value = "\${bcb.pix.url}")
interface BcbClient {

    @Post(
        "/api/v1/pix/keys",
        produces = [MediaType.APPLICATION_XML],
        consumes = [MediaType.APPLICATION_XML]
    )
    fun registra(@Body request: BcbPixRequest): HttpResponse<BcbPixResponse>

    @Delete(
        "/api/v1/pix/keys/{key}",
        produces = [MediaType.APPLICATION_XML],
        consumes = [MediaType.APPLICATION_XML]
    )
    fun deleta(
        @PathVariable key: String,
        @Body request: BcbPixDeleteRequest
    ): HttpResponse<BcbPixDeleteResponse>

    @Get(
        "/api/v1/pix/keys/{key}",
        consumes = [MediaType.APPLICATION_XML]
    )
    fun findByKey(@PathVariable key: String): HttpResponse<PixKeyDetailsResponse>

}

data class BankAccount(
    /**
     * (participant) - ISPB (Identificador de Sistema de Pagamento Brasileiro) do ITAÚ UNIBANCO S.A --> [60701190].
     **/
    val participant: String,
    val branch: String,
    val accountNumber: String,
    val accountType: AccountType
)

data class PixKeyDetailsResponse(
    val keyType: PixKeyTypeEnum,
    val key: String,
    val bankAccount: BankAccount,
    val owner: Owner,
    val createdAt: LocalDateTime
) {

    fun toModel(): ChavePixInfo {
        return ChavePixInfo(
            tipo = keyType.domainType!!,
            chave = this.key,
            tipoDeConta = when (this.bankAccount.accountType) {
                AccountType.CACC -> TipoDeContaEnum.CONTA_CORRENTE
                AccountType.SVGS -> TipoDeContaEnum.CONTA_POUPANCA
            },
            conta = ContaAssociada(
                instituicao = Instituicoes.nome(bankAccount.participant),
                nomeDoTitular = owner.name,
                cpfDoTitular = owner.taxIdNumber,
                agencia = bankAccount.branch,
                numeroDaConta = bankAccount.accountNumber
            )
        )
    }
}

data class BcbPixDeleteRequest(
    val key: String,
    val participant: String = ContaAssociada.ITAU_UNIBANCO_ISPB,
)

data class BcbPixDeleteResponse(
    val key: String,
    val participant: String,
    val deletedAt: LocalDateTime
)

data class BcbPixRequest(
    @field:NotNull
    @Enumerated(EnumType.STRING)
    val keyType: PixKeyTypeEnum,

    @field:NotBlank
    val key: String,

    @field:NotNull
    val bankAccount: BankAccount,

    @field:NotNull
    val owner: Owner,
) {
    companion object {
        fun toClient(chavePix: ChavePix): BcbPixRequest {
            return BcbPixRequest(
                keyType = PixKeyTypeEnum.by(chavePix.tipo),
                key = chavePix.chave,
                bankAccount = BankAccount(
                    participant = ContaAssociada.ITAU_UNIBANCO_ISPB,
                    branch = chavePix.conta.agencia,
                    accountNumber = chavePix.conta.numeroDaConta,
                    accountType = AccountType.by(chavePix.tipoDeConta)
                ),
                owner = Owner(
                    type = Type.NATURAL_PERSON,
                    name = chavePix.conta.nomeDoTitular,
                    taxIdNumber = chavePix.conta.cpfDoTitular
                )
            )
        }
    }
}

enum class Type {
    NATURAL_PERSON
}

data class Owner(
    val type: Type,
    val name: String,
    val taxIdNumber: String,
)

data class BcbPixResponse(
    val keyType: PixKeyTypeEnum,
    val key: String,
    val bankAccount: BankAccount,
    val owner: Owner,
    val createdAt: LocalDateTime,
)


enum class AccountType {
    CACC,
    SVGS;

    companion object {
        fun by(tipoDeconta: TipoDeContaEnum): AccountType {
            return when (tipoDeconta) {
                TipoDeContaEnum.CONTA_CORRENTE -> CACC
                TipoDeContaEnum.CONTA_POUPANCA -> SVGS
            }
        }
    }
}

enum class PixKeyTypeEnum(val domainType: TipoDeChaveEnum?) {
    CPF(domainType = TipoDeChaveEnum.CPF),
    CNPJ(domainType = null),
    PHONE(domainType = TipoDeChaveEnum.CELULAR),
    EMAIL(domainType = TipoDeChaveEnum.EMAIL),
    RANDOM(domainType = TipoDeChaveEnum.ALEATORIA);

    companion object {
        private val mapping: Map<TipoDeChaveEnum?, PixKeyTypeEnum> =
            PixKeyTypeEnum.values().associateBy(PixKeyTypeEnum::domainType)

        fun by(domainTypeValue: TipoDeChaveEnum): PixKeyTypeEnum {
            return mapping[domainTypeValue] ?: throw IllegalArgumentException("O tipo da chave PIX é inválido.")
        }
    }
}