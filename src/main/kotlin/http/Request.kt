package http

/**
 * @version $Id$
 */
public data class Request(
        val path: String
)

class RequestBuilder() {

    private var path = ""

    fun path(path: String) {
        this.path = path
    }

    fun build(): Request {
        return Request(
                path = this.path
        )
    }
}

inline fun request(fn: RequestBuilder.() -> Unit): Request {
    val builder = RequestBuilder()
    builder.fn()
    return builder.build()
}
