package br.com.zup.edu.pix.registra

import br.com.zup.edu.KeyManagerRegistraGrpcServiceGrpc
import br.com.zup.edu.RegistraChavePixRequest
import br.com.zup.edu.RegistraChavePixResponse
import br.com.zup.edu.exception.ErrorHandler
import br.com.zup.edu.toModel
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton

@ErrorHandler
@Singleton
class RegistraChaveEndpoint(@Inject private val service: NovaChavePixService)
    : KeyManagerRegistraGrpcServiceGrpc.KeyManagerRegistraGrpcServiceImplBase() {

    override fun registra(
        request: RegistraChavePixRequest?,
        responseObserver: StreamObserver<RegistraChavePixResponse>?
    ) {

        val novaChave = request?.toModel() // recebe do bloomrpc e coverte num dto a partir da classe gerada pelo protobuf
        val chaveCriada = service.registra(novaChave!!) // passa o dto p/ classe service, que faz a lógica e salva no bd

        responseObserver?.onNext(
            RegistraChavePixResponse.newBuilder()
            .setClienteId(chaveCriada.clienteId.toString())
            .setPixId(chaveCriada.id.toString())
            .build())
        responseObserver?.onCompleted()
    }
}