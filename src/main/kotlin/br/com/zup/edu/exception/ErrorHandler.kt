package br.com.zup.edu.exception

import io.micronaut.aop.Around
import io.micronaut.context.annotation.Type

@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
@Around
@Type(ExceptionHandlerInterceptor::class)
annotation class ErrorHandler()