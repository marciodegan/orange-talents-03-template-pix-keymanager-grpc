package br.com.zup.edu.pix.registra

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class TipoDeChaveEnumTest {

    @Nested
    inner class ALEATORIA {

        @Test
        fun `deve ser valido qdo chave aleatoria for nula ou vazia`() {
            with(TipoDeChaveEnum.ALEATORIA) {
                assertTrue(valida(null))
                assertTrue(valida(""))
            }
        }


        @Test
        fun `nao deve ser valido qdo chave aleatoria possuir um valor`() {
            with(TipoDeChaveEnum.ALEATORIA) {
                assertFalse(valida("um valor qualquer"))
            }
        }
    }

    @Nested
    inner class CPF {
        @Test
        fun `deve ser valido qdo cpf for um numero valido`() {
            with(TipoDeChaveEnum.CPF) {
                assertTrue(valida("96275049049"))
            }
        }

        @Test
        fun `deve ser invalido qdo cpf for um numero invalido`() {
            with(TipoDeChaveEnum.CPF) {
                assertFalse(valida("12345678901"))
            }
        }

        @Test
        fun `nao deve ser valido qdo cpf nao for informado`() {
            with(TipoDeChaveEnum.CPF) {
                assertFalse(valida(null))
                assertFalse(valida(""))
            }
        }
    }

    @Nested
    inner class EMAIL {
        @Test
        fun `deve ser valido qdo email for valido`() {
            with(TipoDeChaveEnum.EMAIL) {
                assertTrue(valida("teste@teste.com"))
            }
        }

        @Test
        fun `nao deve ser valido qdo email estiver em um formato invalido`() {
            with(TipoDeChaveEnum.EMAIL) {
                assertFalse(valida("teste.com.br"))
                assertFalse(valida("teste@teste.com."))
            }
        }

        @Test
        fun `nao deve ser valido qdo email nao for informado`() {
            with(TipoDeChaveEnum.EMAIL) {
                assertFalse(valida(null))
                assertFalse(valida(""))
            }
        }
    }
}