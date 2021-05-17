package br.com.zup.edu.pix

import org.hibernate.validator.internal.constraintvalidators.hv.EmailValidator
import org.hibernate.validator.internal.constraintvalidators.hv.br.CPFValidator

enum class TipoDeChaveEnum {

    CPF {
        override fun valida(chave: String?): Boolean {
            if(chave.isNullOrBlank()) return false

            if(!chave.matches(regex = Regex("^[0-9]{11}\$"))) return false

            return CPFValidator().run {
                initialize(null)
                isValid(chave, null)
            }
        }
    },
    CELULAR {
        override fun valida(chave: String?): Boolean {
            if(chave.isNullOrBlank()) {
                return false
            }
            return chave.matches("^\\+[1-9][0-9]\\d{1,14}\$".toRegex())
        }
    },
    EMAIL {
        override fun valida(chave: String?): Boolean {
            if(chave.isNullOrBlank()){
                return false
            }

            return  EmailValidator().run {
                initialize(null)
                isValid(chave, null)
            }
        }
    },

    ALEATORIA {
        override fun valida(chave: String?) = chave.isNullOrBlank()
    };

     /*método abstrato que recebe a chave e retorna se é valida ou não
     obriga cada enum implementá-lo, permitindo uma regra específica p/ cada*/
    abstract fun valida(chave: String?): Boolean
}

// se no futuro, for necessário novo tipo de chave, basta implementar suas regras.
