package br.com.zup.edu.exception
import io.grpc.Metadata

import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.grpc.protobuf.StatusProto

/**
 * https://gist.github.com/rponte/949d947ac3c38aa7181929c41ee56c05#file-a03_exceptionhandler-kt
 **/
interface ExceptionHandler<E : Exception> {
    /**
     * Handles exception and maps it to StatusWithDetails
     */
    fun handle(e: E): StatusWithDetails

    /**
     * Verifies whether this instance can handle the specified exception or not
     */
    fun supports(e: Exception): Boolean

    /**
     * Simple wrapper for Status and Metadata (trailers)
     */
    data class StatusWithDetails(val status: Status, val metadata: Metadata = Metadata()) {
        constructor(se: StatusRuntimeException) : this(se.status, se.trailers ?: Metadata())
        constructor(sp: com.google.rpc.Status) : this(StatusProto.toStatusRuntimeException(sp))

        fun asRuntimeException(): StatusRuntimeException {
            return status.asRuntimeException(metadata)
        }
    }
}