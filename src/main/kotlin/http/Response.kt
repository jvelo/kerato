package http

/**
 * @version $Id$
 */

public interface Response {
    val status: Int
    val headers: Map<String, String>
    val body: Any
    val halted: Boolean

    fun with(fn: ResponseBuilder.() -> Unit): Response
    fun halt(fn: ResponseBuilder.() -> Unit): Response

    fun type(): String
}

public interface ResponseBuilder {
    fun merge(other: Response): ResponseBuilder
    fun status(status: Status): ResponseBuilder
    fun status(status: Int): ResponseBuilder
    fun body(body: Any): ResponseBuilder
    fun json(json: Any): ResponseBuilder
    fun header(name: String, value: String): ResponseBuilder
    fun header(header: Pair<String, String>): ResponseBuilder
    fun headers(vararg headers: Pair<String, String>): ResponseBuilder
    fun halt(): ResponseBuilder
    fun build(): Response
}

public open data class BaseResponse(
        override val status: Int,
        override val headers: Map<String, String>,
        override val body: Any,
        override val halted: Boolean = false
) : Response {

    override final inline fun with(fn: ResponseBuilder.() -> Unit): Response {
        val builder = DefaultResponseBuilder(this)
        builder.fn()
        return builder.build()
    }

    override final inline fun halt(fn: ResponseBuilder.() -> Unit): Response {
        val builder = DefaultResponseBuilder(this)
        builder.fn()
        builder.halt()
        return builder.build()
    }

    override fun type() : String {
        return this.headers.get("Content-Type") ?: "text/plain"
    }
}

public data class CopiedResponse(status: Int, headers: Map<String, String>, body: Any, halted: Boolean) :
        BaseResponse(status, headers, body, halted) {}

class DefaultResponseBuilder() : ResponseBuilder {

    constructor(other: Response) : this() {
        this.status = other.status
        this.headers.putAll(other.headers)
        this.body = other.body
        this.halted = other.halted
        this.copied = true
    }

    override fun merge(other: Response): ResponseBuilder {
        if (other.status !== defaultStatus) status = other.status
        if (other.body !== defaultBody) body = other.body
        if (other.halted !== defaultHalted) halted = other.halted
        headers.putAll(other.headers)
        return this;
    }

    private val defaultStatus: Int = Status.OK.code
    private val defaultHeaders: MutableMap<String, String> = hashMapOf()
    private val defaultBody: Any = ""
    private val defaultHalted: Boolean = false

    private var status: Int = defaultStatus
    private val headers: MutableMap<String, String> = defaultHeaders
    private var body: Any = defaultBody
    private var halted: Boolean = defaultHalted

    private var copied = false

    override fun status(status: Status): ResponseBuilder {
        this.status = status.code
        return this
    }

    override fun status(status: Int): ResponseBuilder {
        this.status = status
        return this
    }

    override fun body(body: Any): ResponseBuilder {
        this.body = body
        return this
    }

    override fun json(json: Any): ResponseBuilder {
        body(json)
        header("Content-Type", "application/json")
        return this
    }

    override fun header(name: String, value: String): ResponseBuilder {
        this.headers.putAll(Pair(name, value))
        return this
    }

    override fun header(header: Pair<String, String>): ResponseBuilder {
        this.headers.put(header.first, header.second)
        return this
    }

    override fun headers(vararg headers: Pair<String, String>): ResponseBuilder {
        for (header in headers) {
            this.header(header)
        }
        return this
    }

    override fun halt(): ResponseBuilder {
        halted = true
        return this
    }

    override fun build(): Response {
        return when (this.copied) {
            true -> CopiedResponse(
                    status = this.status,
                    headers = this.headers,
                    body = this.body,
                    halted = this.halted
            )
            else -> BaseResponse(
                    status = this.status,
                    headers = this.headers,
                    body = this.body,
                    halted = this.halted
            )
        }
    }

}

inline fun response(fn: ResponseBuilder.() -> Unit): Response {
    val builder = DefaultResponseBuilder()
    builder.fn()
    return builder.build()
}

fun ok() : Response {
    return response {}
}

fun seeOther(uri: String): Response {
    return response {
        status(Status.SEE_OTHER)
        header("Location" to uri)
    }
}
