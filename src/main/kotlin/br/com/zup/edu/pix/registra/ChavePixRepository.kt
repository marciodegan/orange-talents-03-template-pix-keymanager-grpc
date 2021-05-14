package br.com.zup.edu.pix.registra

import br.com.zup.edu.pix.registra.ChavePix
import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository

@Repository
interface ChavePixRepository: JpaRepository<ChavePix, Long> {
    fun existsByChave(value: String?): Boolean
}

