package br.com.zup.edu.pix

import br.com.zup.edu.pix.ChavePix
import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository
import java.util.*

@Repository
interface ChavePixRepository : JpaRepository<ChavePix, UUID> {
    fun existsByChave(value: String?) : Boolean
//    fun existsByIdAndClienteId(id: UUID, clienteId: UUID) : Boolean
    fun findByIdAndClienteId(id: UUID, clienteId: UUID) : Optional<ChavePix>
    fun findByChave(chave: String): Optional<ChavePix>
    fun findAllByClienteId(clienteId: UUID): List<ChavePix>

}