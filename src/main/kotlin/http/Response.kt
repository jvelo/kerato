package http

/**
 * @version $Id$
 */

public data class Response(
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

class ResponseBuilder() {

    constructor(other: Response) : this() {
        this.status = other.status
        this.headers.putAll(other.headers)
    }

    private var status: Int = Status.OK.code
    private val headers: MutableMap<String, String> = hashMapOf()
    private var body: Any = ""
    private var halted: Boolean = false

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
        return Response(
                status = this.status,
                headers = this.headers,
                body = this.body
        )
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
