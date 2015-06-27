package http

/**
 * @version $Id$
 */

public interface Request {
    val path: String
    val method: Method
}

public data class BaseRequest(
        override val path: String,
        override val method: Method
) : Request

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
        return BaseRequest(
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
