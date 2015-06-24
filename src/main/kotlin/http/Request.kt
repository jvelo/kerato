package http

/**
 * @version $Id$
 */
public data class Request(
    val path: String,
    val method: Method
)

class RequestBuilder() {

    private var path = ""
    private var method = Method.GET

    fun path(path: String) {
        this.path = path
    }

    fun method(method: Method) {
        this.method = method
    }

    fun build(): Request {
        return Request(
                path = this.path,
                method = this.method
        )
    }
}

inline fun request(fn: RequestBuilder.() -> Unit): Request {
    val builder = RequestBuilder()
    builder.fn()
    return builder.build()
}
