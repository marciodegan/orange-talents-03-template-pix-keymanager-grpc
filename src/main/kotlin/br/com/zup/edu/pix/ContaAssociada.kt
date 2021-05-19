package br.com.zup.edu.pix

import io.micronaut.core.annotation.Introspected
import javax.persistence.Embeddable

@Embeddable
@Introspected
class ContaAssociada(
    val instituicao: String = "",
    val nomeDoTitular: String = "",
    val cpfDoTitular: String = "",
    val agencia: String = "",
    val numeroDaConta: String= ""
) {
    companion object {
        public val ITAU_UNIBANCO_ISPB: String = "60701190"
    }
}
