package br.com.zup.edu.pix.remove

import br.com.zup.edu.KeyManagerRegistraGrpcServiceGrpc
import br.com.zup.edu.KeyManagerRemoveGrpcServiceGrpc
import br.com.zup.edu.RemoveChavePixRequest
import br.com.zup.edu.RemoveChavePixResponse
import br.com.zup.edu.exception.ErrorHandler
import br.com.zup.edu.pix.ChavePixRepository
import io.grpc.stub.StreamObserver
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Delete
import io.micronaut.http.annotation.PathVariable
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional

@ErrorHandler
@Singleton
class RemoveChaveEndpoint(@Inject private val service: RemoveChavePixService)
    : KeyManagerRemoveGrpcServiceGrpc.KeyManagerRemoveGrpcServiceImplBase(){

    override fun remove(
        request: RemoveChavePixRequest?,
        responseObserver: StreamObserver<RemoveChavePixResponse>?
    ) {
        service.deleta(clienteId = request?.clienteId, pixId = request?.pixId)

        responseObserver?.onNext(RemoveChavePixResponse.newBuilder()
            .setClienteId(request?.clienteId)
            .setPixId(request?.pixId)
            .build())
        responseObserver?.onCompleted()
        responseObserver?.onCompleted()
    }
}