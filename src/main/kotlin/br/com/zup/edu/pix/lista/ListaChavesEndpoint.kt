package br.com.zup.edu.pix.lista

import br.com.zup.edu.*
import br.com.zup.edu.exception.ErrorHandler
import br.com.zup.edu.pix.ChavePixRepository
import com.google.protobuf.Timestamp
import io.grpc.stub.StreamObserver
import java.lang.IllegalArgumentException
import java.time.ZoneId
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@ErrorHandler
@Singleton
class ListaChavesEndpoint(@Inject private val repository: ChavePixRepository) :
    KeyManagerListaGrpcServiceGrpc.KeyManagerListaGrpcServiceImplBase() {

    override fun lista(
        request: ListaChavesPixRequest?,
        responseObserver: StreamObserver<ListaChavesPixResponse>?
    ) {
        if (request?.clienteId.isNullOrBlank())
            throw IllegalArgumentException("Cliente ID não pode ser nulo ou vazio")

        val clienteId = UUID.fromString(request?.clienteId)
        val chaves = repository.findAllByClienteId(clienteId).map { // o findAllByClient retorna lista vazia
            ListaChavesPixResponse.ChavePix.newBuilder()
                .setPixId(it.id.toString())
                .setTipo(TipoDeChave.valueOf(it.tipo.name))
                .setChave(it.chave)
                .setTipoDeConta(TipoDeConta.valueOf(it.tipoDeConta.name))
                .setCriadaEm(it.criadaEm.let {
                    val createdAt = it.atZone(ZoneId.of("UTC")).toInstant()
                    Timestamp.newBuilder()
                        .setSeconds(createdAt.epochSecond)
                        .setNanos(createdAt.nano)
                        .build()
                })
                .build()
        }
        responseObserver?.onNext(
            ListaChavesPixResponse.newBuilder()
                .setClienteId(clienteId.toString())
                .addAllChaves(chaves)
                .build()
        )
        responseObserver?.onCompleted()


    }


}
