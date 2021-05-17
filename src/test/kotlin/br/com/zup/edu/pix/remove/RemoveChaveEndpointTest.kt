package br.com.zup.edu.pix.remove

import br.com.zup.edu.KeyManagerRemoveGrpcServiceGrpc
import br.com.zup.edu.RemoveChavePixRequest
import br.com.zup.edu.pix.*
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import java.util.*
import javax.inject.Inject

@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@MicronautTest(transactional = false)
internal class RemoveChaveEndpointTest(
    val keyManager: KeyManagerRemoveGrpcServiceGrpc.KeyManagerRemoveGrpcServiceBlockingStub
) {

    @field:Inject
    lateinit var repository: ChavePixRepository

    lateinit var CHAVE_EXISTENTE: ChavePix

    @BeforeEach
    fun setup() {
        CHAVE_EXISTENTE = repository.save(ChavePix(
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
        ))
    }

    @AfterEach
    fun cleanUp() {
        repository.deleteAll()
    }

    @Test @Order(1)
    fun `deve remover chave existente`() {
        // acao
        val response = keyManager.remove(
            RemoveChavePixRequest.newBuilder()
                .setPixId(CHAVE_EXISTENTE.id.toString())
                .setClienteId(CHAVE_EXISTENTE.clienteId.toString())
                .build()
        )
        //validacao
        assertEquals(CHAVE_EXISTENTE.clienteId.toString(), response.clienteId)
        assertEquals(CHAVE_EXISTENTE.id.toString(), response.pixId)
    }

    @Test @Order(2)
    fun `nao deve remover chave pix qdo chave inexistente`() {
        // cenario
        val pixIdNaoExistente = UUID.randomUUID().toString()

        //acao
        val exceptionThrown = assertThrows<StatusRuntimeException> {
            keyManager.remove(RemoveChavePixRequest.newBuilder()
                    .setPixId(pixIdNaoExistente)
                    .setClienteId(CHAVE_EXISTENTE.clienteId.toString())
                    .build())
        }
        //validacao
        with(exceptionThrown){
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Chave pix ou clienteID não encontrados", status.description)
        }
    }

    @Test @Order(3)
    fun `nao deve remover chave pix qdo chave existente mas pertence a outro cliente`() {

        val clientIdNaoExistente = UUID.randomUUID().toString()

        // acao
        val exceptionThrown = assertThrows<StatusRuntimeException> {
            keyManager.remove(RemoveChavePixRequest.newBuilder()
                .setPixId(CHAVE_EXISTENTE.id.toString())
                .setClienteId(clientIdNaoExistente)
                .build())
        }
        // validation
        with(exceptionThrown){
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Chave pix ou clienteID não encontrados", status.description)
        }
    }

    @Factory
    class Clients {
        @Bean
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeyManagerRemoveGrpcServiceGrpc.KeyManagerRemoveGrpcServiceBlockingStub {
            return KeyManagerRemoveGrpcServiceGrpc.newBlockingStub(channel)
        }
    }
}