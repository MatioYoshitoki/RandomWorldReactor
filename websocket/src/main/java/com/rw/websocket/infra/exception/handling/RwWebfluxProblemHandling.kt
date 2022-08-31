package com.rw.websocket.infra.exception.handling

import com.rw.websocket.infra.contants.ErrorConstants
import com.rw.websocket.infra.exception.BadRequestAlertException
import com.rw.websocket.infra.utils.HeaderUtil
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.support.WebExchangeBindException
import org.springframework.web.server.ServerWebExchange
import org.zalando.problem.*
import org.zalando.problem.spring.webflux.advice.ProblemHandling
import org.zalando.problem.spring.webflux.advice.security.SecurityAdviceTrait
import org.zalando.problem.violations.ConstraintViolationProblem
import reactor.core.publisher.Mono
import javax.annotation.Nonnull

@ControllerAdvice
@Component
class RwWebfluxProblemHandling : ProblemHandling, SecurityAdviceTrait, AccessTokenInvalidExceptionAdviceTrait {
    private val applicationName: String = "rw-websocket"
    private val log = LoggerFactory.getLogger(javaClass)
    override fun process(
        entity: ResponseEntity<Problem>?, request: ServerWebExchange
    ): Mono<ResponseEntity<Problem>> {
        if (entity == null) {
            return Mono.empty()
        }
        log.error("WebFlux 错误处理，错误信息：$entity")
        val problem: Problem? = entity.body
        if (!(problem is ConstraintViolationProblem || problem is DefaultProblem)) {
            return Mono.just(entity)
        }
        val builder: ProblemBuilder = Problem.builder()
            .withType(if (Problem.DEFAULT_TYPE == problem.type) ErrorConstants.DEFAULT_TYPE else problem.type)
            .withStatus(problem.status).withTitle(problem.title).with(HOST_KEY, applicationName)
            .with(PATH_KEY, request.request.path.value())
        if (problem is ConstraintViolationProblem) {
            builder.with(VIOLATIONS_KEY, problem.violations).with(MESSAGE_KEY, ErrorConstants.ERR_VALIDATION)
        } else {
            builder.withCause((problem as DefaultProblem).cause).withDetail(problem.detail)
                .withInstance(problem.instance)
            problem.parameters.forEach { (key: String?, value: Any?) -> builder.with(key, value) }
            if (!problem.parameters.containsKey(MESSAGE_KEY) && problem.status != null) {
                builder.with(MESSAGE_KEY, "error.http." + problem.status!!.statusCode)
            }
        }
        return Mono.just(
            ResponseEntity<Problem>(
                builder.build(), entity.headers, entity.statusCode
            )
        )
    }

    override fun handleBindingResult(
        ex: WebExchangeBindException, @Nonnull request: ServerWebExchange
    ): Mono<ResponseEntity<Problem>> {
        val result: BindingResult = ex.bindingResult
        val fieldErrors = result.fieldErrors.map {
            FieldErrorVM(it.objectName.replaceFirst("DTO$".toRegex(), ""), it.field, it.code)
        }
        val problem: Problem = Problem.builder().withType(ErrorConstants.CONSTRAINT_VIOLATION_TYPE)
            .withTitle("Data binding and validation failure").withStatus(Status.BAD_REQUEST)
            .with(MESSAGE_KEY, ErrorConstants.ERR_VALIDATION).with(FIELD_ERRORS_KEY, fieldErrors).build()
        return create(ex, problem, request)
    }

    /*
    @Override
    public Mono<ResponseEntity<Problem>> handleThrowable(Throwable throwable, ServerWebExchange request) {
        Problem problem = Problem.builder()
                .withType(ErrorConstants.DEFAULT_TYPE)
                .withTitle("Server Error")
                .withStatus(Status.INTERNAL_SERVER_ERROR)
                .with(MESSAGE_KEY, throwable.getMessage())
                .withInstance(request.getRequest().getURI())
                .build();
        return create(throwable, problem, request);
    }
     */
    @ExceptionHandler
    fun handleBadRequestAlertException(
        ex: BadRequestAlertException, request: ServerWebExchange
    ): Mono<ResponseEntity<Problem>> {
        return create(
            ex, request, HeaderUtil.createFailureAlert(applicationName, false, ex.entityName, ex.errorKey, ex.message)
        )
    }

    override fun toProblem(throwable: Throwable, status: HttpStatus): ThrowableProblem {
        return Problem.builder().withType(ErrorConstants.DEFAULT_TYPE).withTitle(throwable.message)
            .withStatus(Status.INTERNAL_SERVER_ERROR).with(MESSAGE_KEY, throwable.message ?: "").build()
    }

    companion object {
        private const val FIELD_ERRORS_KEY = "fieldErrors"
        private const val MESSAGE_KEY = "message"
        private const val PATH_KEY = "path"
        private const val VIOLATIONS_KEY = "violations"
        private const val HOST_KEY = "host"
    }
}
