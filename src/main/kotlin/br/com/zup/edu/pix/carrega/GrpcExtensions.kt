package br.com.zup.edu.pix.carrega

import br.com.zup.edu.CarregaChavePixRequest
import javax.validation.ConstraintViolationException
import javax.validation.Validator
import br.com.zup.edu.CarregaChavePixRequest.FiltroCase.*


fun CarregaChavePixRequest.toModel(validator: Validator): Filtro { // 1

    val filtro = when(filtroCase!!) { // 1
        PIXID -> pixId.let { // 1
            Filtro.PorPixId(clienteId = it.clienteId, pixId = it.pixId) // 1
        }
        CHAVE -> Filtro.PorChave(chave) // 2
        FILTRO_NOT_SET -> Filtro.Invalido() // 2
    }

    val violations = validator.validate(filtro)
    if (violations.isNotEmpty()) {
        throw ConstraintViolationException(violations)
    }

    return filtro
}