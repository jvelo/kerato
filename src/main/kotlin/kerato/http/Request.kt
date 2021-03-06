package kerato.http

import kerato.conversions.Conversions
import java.io.InputStream
import java.net.InetSocketAddress

/**
 * @version $Id$
 */

public data class Request(
        val path: String = "",
        val method: HttpMethod = HttpMethod.GET,
        val pathParameters: Map<String, String> = mapOf(),
        val clientAddress: InetSocketAddress = InetSocketAddress.createUnresolved("0.0.0.0", 80),
        val payload: InputStream? = null
) {
    val content: String?
        get() {
            val reader = this.payload?.bufferedReader() ?: return null
            try {
                return reader.readText()
            } finally {
                reader.close()
            }
        }

    fun pathParameter(name: String): String? = pathParameters.get(name)

    suppress("BASE_WITH_NULLABLE_UPPER_BOUND")
    final inline fun <reified T> pathParameterAs(name: String): T? {
        val value = pathParameter(name)

        when (value) {
            null -> return null
            is T -> return value
        }

        return Conversions.fromString(value!!, javaClass<T>())
    }

    final inline fun with(fn: RequestBuilder.() -> Unit): Request {
        val builder = DefaultRequestBuilder(this)
        builder.fn()
        return builder.build()
    }
}

public interface RequestBuilder {
    fun path(path: String): RequestBuilder
    fun method(method: HttpMethod): RequestBuilder
    fun pathParameter(name: String, value: String): RequestBuilder
    fun payload(payload: InputStream): RequestBuilder
    fun clientAddress(clientAddress: InetSocketAddress): RequestBuilder

    fun build(): Request
}

class DefaultRequestBuilder(var request: Request = Request()) : RequestBuilder {

    override fun path(path: String): RequestBuilder {
        request = request.copy(path = path)
        return this
    }

    override fun method(method: HttpMethod): RequestBuilder {
        request = request.copy(method = method)
        return this
    }

    override fun pathParameter(name: String, value: String): RequestBuilder {
        request = request.copy(pathParameters = request.pathParameters.plus(Pair(name, value)))
        return this
    }

    override fun payload(payload: InputStream): RequestBuilder {
        request = request.copy(payload = payload)
        return this
    }

    override fun clientAddress(clientAddress: InetSocketAddress): RequestBuilder {
        request = request.copy(clientAddress = clientAddress)
        return this
    }

    override fun build(): Request {
        return this.request
    }
}

inline fun request(fn: RequestBuilder.() -> Unit): Request {
    val builder = DefaultRequestBuilder()
    builder.fn()
    return builder.build()
}
