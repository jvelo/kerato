package http

/**
 * @version $Id$
 */

public open data class Response(
        val status: Int,
        val headers: Map<String, String>,
        val body: Any,
        val halted: Boolean = false
) {
    inline fun with(fn: ResponseBuilder.() -> Unit): Response {
        val builder = ResponseBuilder(this)
        builder.fn()
        return builder.build()
    }

    inline fun halt(fn: ResponseBuilder.() -> Unit): Response {
        val builder = ResponseBuilder(this)
        builder.fn()
        builder.halt()
        return builder.build()
    }
}

public data class CopiedResponse(status: Int, headers: Map<String, String>, body: Any, halted: Boolean) :
        Response(status, headers, body, halted) {}

class ResponseBuilder() {

    constructor(other: Response) : this() {
        this.status = other.status
        this.headers.putAll(other.headers)
        this.body = other.body
        this.halted = other.halted
        this.copied = true
    }

    fun merge(other: Response): ResponseBuilder {
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

    fun status(status: Status) {
        this.status = status.code
    }

    fun status(status: Int) {
        this.status = status
    }

    fun body(body: Any) {
        this.body = body
    }

    fun header(name: String, value: String) {
        this.headers.putAll(Pair(name, value))
    }

    fun header(header: Pair<String, String>) {
        this.headers.put(header.first, header.second)
    }

    fun headers(vararg headers: Pair<String, String>) {
        for (header in headers) {
            this.header(header)
        }
    }

    fun halt() {
        halted = true
    }

    fun build(): Response {
        if (this.copied) {
            return CopiedResponse(
                    status = this.status,
                    headers = this.headers,
                    body = this.body,
                    halted = this.halted
            )
        } else {
            return Response(
                    status = this.status,
                    headers = this.headers,
                    body = this.body,
                    halted = this.halted
            )
        }
    }

}

inline fun response(fn: ResponseBuilder.() -> Unit): Response {
    val builder = ResponseBuilder()
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
