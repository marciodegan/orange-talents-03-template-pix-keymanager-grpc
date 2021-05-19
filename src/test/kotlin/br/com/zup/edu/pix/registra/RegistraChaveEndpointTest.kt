package br.com.zup.edu.pix.registra

import br.com.zup.edu.*
import br.com.zup.edu.pix.*
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpResponse.*
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.mockito.Mockito
import org.mockito.Mockito.*
import java.util.*
import javax.inject.Inject

@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@MicronautTest(transactional = false)
class CadastraChaveGrpcEndpointTest {

    @field:Inject
    lateinit var chavePixRepository: ChavePixRepository

    @field:Inject
    lateinit var itauClient: ContasDeClientesNoItauClient

    @field:Inject
    lateinit var keyManagerServiceGrpc: KeyManagerRegistraGrpcServiceGrpc.KeyManagerRegistraGrpcServiceBlockingStub

    @BeforeEach
    internal fun setUp() {
        // clean db
        chavePixRepository.deleteAll()
    }

    companion object {
        val dadosContaResponse = DadosDaContaResponse(
            tipo = "CONTA_CORRENTE",
            instituicao = InstituicaoResponse("UNIBANCO ITAU SA", "ITAU_UNIBANCO_ISPB"),
            agencia = "1234",
            numero = "123456",
            titular = TitularResponse("Joao de Maria", cpf = "12345678901")
        )
        val randomClientId = UUID.randomUUID().toString()
    }

    @Test @Order(1)
    fun `deve cadastrar uma nova chave pix do tipo cpf`() {
        // cenario
        `when`(itauClient.buscaContaPorTipo(clienteId = randomClientId, tipo = "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.ok(dadosContaResponse))

        // acao
        val response = keyManagerServiceGrpc.registra(
            RegistraChavePixRequest.newBuilder()
                .setClienteId(randomClientId)
                .setTipoDeChave(RegistraChavePixRequest.TipoDeChave.CPF)
                .setChave("99942051023")
                .setTipoDeConta(RegistraChavePixRequest.TipoDeConta.CONTA_CORRENTE)
                .build()
        )

        // validacao
        with(response) {
            assertTrue(chavePixRepository.existsByChave("99942051023"))
            assertTrue(chavePixRepository.count() == 1L)
            assertNotNull(pixId)
            assertEquals(randomClientId, clienteId)
            verify(itauClient, atMost(1)).buscaContaPorTipo(
                clienteId = randomClientId,
                tipo = RegistraChavePixRequest.TipoDeConta.CONTA_CORRENTE.name
            )
        }
    }

    @Test @Order(2)
    fun `deve cadastrar uma nova chave pix do tipo email`() {
        // cenario
        `when`(itauClient.buscaContaPorTipo(clienteId = randomClientId, tipo = "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.ok(dadosContaResponse))

        // acao
        val response = keyManagerServiceGrpc.registra(
            RegistraChavePixRequest.newBuilder()
                .setClienteId(randomClientId)
                .setTipoDeChave(RegistraChavePixRequest.TipoDeChave.EMAIL)
                .setChave("teste@teste.com")
                .setTipoDeConta(RegistraChavePixRequest.TipoDeConta.CONTA_CORRENTE)
                .build()
        )
        // validacao
        with(response) {
            assertTrue(chavePixRepository.existsByChave("teste@teste.com"))
            assertTrue(chavePixRepository.count() == 1L)
            assertNotNull(pixId)
            assertEquals(randomClientId, clienteId)
            verify(itauClient, atMost(1)).buscaContaPorTipo(
                clienteId = randomClientId,
                tipo = RegistraChavePixRequest.TipoDeConta.CONTA_CORRENTE.name
            )
        }
    }

    @Test @Order(3)
    fun `deve cadastrar uma nova chave pix do tipo celular`() {
        // cenario
        `when`(itauClient.buscaContaPorTipo(clienteId = randomClientId, tipo = "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.ok(dadosContaResponse))

        // acao
        val response = keyManagerServiceGrpc.registra(
            RegistraChavePixRequest.newBuilder()
                .setClienteId(randomClientId)
                .setTipoDeChave(RegistraChavePixRequest.TipoDeChave.CELULAR)
                .setChave("+5585999999999")
                .setTipoDeConta(RegistraChavePixRequest.TipoDeConta.CONTA_CORRENTE)
                .build()
        )

        // validacao
        with(response) {
            assertTrue(chavePixRepository.existsByChave("+5585999999999"))
            assertTrue(chavePixRepository.count() == 1L)
            assertNotNull(pixId)
            assertEquals(randomClientId, clienteId)
            verify(itauClient, atMost(1)).buscaContaPorTipo(
                clienteId = randomClientId,
                tipo = RegistraChavePixRequest.TipoDeConta.CONTA_CORRENTE.name
            )
        }
    }

    @Test @Order(4)
    fun `deve cadastrar uma nova chave pix do tipo aleatoria`() {
        `when`(itauClient.buscaContaPorTipo(clienteId = randomClientId, tipo = "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.ok(dadosContaResponse))

        val response = keyManagerServiceGrpc.registra(
            RegistraChavePixRequest.newBuilder()
                .setClienteId(randomClientId)
                .setTipoDeChave(RegistraChavePixRequest.TipoDeChave.ALEATORIA)
                .setChave("")
                .setTipoDeConta(RegistraChavePixRequest.TipoDeConta.CONTA_CORRENTE)
                .build()
        )

        with(response) {
            assertTrue(chavePixRepository.count() == 1L)
            assertNotNull(pixId)
            assertEquals(randomClientId, clienteId)
            verify(itauClient, atMost(1)).buscaContaPorTipo(
                clienteId = randomClientId,
                tipo = RegistraChavePixRequest.TipoDeConta.CONTA_CORRENTE.name
            )
        }
    }

    @Test @Order(5)
    fun `nao deve registrar chave pix quando chave existente`() {
        chavePixRepository.save(
            ChavePix(
                tipo = TipoDeChaveEnum.EMAIL,
                chave = "teste@teste.com",
                clienteId = UUID.randomUUID(),
                conta = ContaAssociada(
                    instituicao = "ITAU UNIBANCO",
                    cpfDoTitular = "99999999999",
                    agencia = "1234",
                    nomeDoTitular = "Joao de Maria",
                    numeroDaConta = "123456"
                ),
                tipoDeConta = TipoDeContaEnum.CONTA_CORRENTE
            )
        )

        // ação
        val response = assertThrows<StatusRuntimeException> {
            keyManagerServiceGrpc.registra(
                RegistraChavePixRequest.newBuilder()
                    .setClienteId(randomClientId)
                    .setTipoDeChave(RegistraChavePixRequest.TipoDeChave.EMAIL)
                    .setChave("teste@teste.com")
                    .setTipoDeConta(RegistraChavePixRequest.TipoDeConta.CONTA_CORRENTE)
                    .build()
            )
        }

        with(response) {
            assertEquals(Status.ALREADY_EXISTS.code, status.code)
            assertEquals("Chave Pix teste@teste.com já existe", status.description)
        }
    }


    @Test @Order(6)
    fun `nao deve registrar chave quando nao encontrar dados da conta cliente`() {
        // cenario
        `when`(itauClient.buscaContaPorTipo(clienteId = randomClientId, tipo = "CONTA_CORRENTE"))
            .thenReturn(notFound())

        // acao
        val builtException = assertThrows<StatusRuntimeException> {
            keyManagerServiceGrpc.registra(
                RegistraChavePixRequest.newBuilder()
                    .setClienteId(randomClientId)
                    .setTipoDeChave(RegistraChavePixRequest.TipoDeChave.EMAIL)
                    .setChave("teste@teste.com")
                    .setTipoDeConta(RegistraChavePixRequest.TipoDeConta.CONTA_CORRENTE)
                    .build()
            )
        }

        // validacao
        with(builtException) {
            assertEquals(Status.FAILED_PRECONDITION.code, status.code)
            assertEquals("Cliente não encontrado no Itau", status.description)
        }
    }

    @Test @Order(7)
    fun `nao deve registrar chave quando parametros forem invalidos`() {
        // acao
        val builtException = assertThrows<StatusRuntimeException> {
            keyManagerServiceGrpc.registra((RegistraChavePixRequest.newBuilder().build()))
        }
        // validacao
        with(builtException) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
//            assertEquals("Dados inválidos", status.description)
        }
    }

    @MockBean(ContasDeClientesNoItauClient::class)
    fun itauClient(): ContasDeClientesNoItauClient? {
        return Mockito.mock(ContasDeClientesNoItauClient::class.java)
    }

    @Factory
    class Clients {
        @Bean
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeyManagerRegistraGrpcServiceGrpc.KeyManagerRegistraGrpcServiceBlockingStub {
            return KeyManagerRegistraGrpcServiceGrpc.newBlockingStub(channel)
        }
    }
}