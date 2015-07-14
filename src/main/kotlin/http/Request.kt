package http

import kotlin.reflect.KClass

/**
 * @version $Id$
 */

public interface Request {
    val path: String
    val method: Method
    val pathParameters: Map<String, String>

    fun pathParameter(name: String): String?
    fun <T> pathParameterAs(name: String): T?

    fun with(fn: RequestBuilder.() -> Unit): Request
}

public interface RequestBuilder {
    fun path(path: String): RequestBuilder
    fun method(method: Method): RequestBuilder
    fun pathParameter(name: String, value: String): RequestBuilder

    fun build(): Request
}

public data class BaseRequest(
        override val path: String,
        override val method: Method,
        override val pathParameters: Map<String, String>
) : Request {

    override final inline fun <reified T> pathParameterAs(name: String): T {
        val value = pathParameters.get(name)
        val valueOfMethod = javaClass<T>().getMethod("valueOf", javaClass<String>())
        return when {
            value is T -> value
            valueOfMethod != null -> valueOfMethod.invoke(javaClass<T>().newInstance(), value) as T
            else -> null
        }
    }

    override fun pathParameter(name: String) = pathParameters.get(name)

    override fun with(fn: RequestBuilder.() -> Unit): Request {
        val builder = DefaultRequestBuilder(this)
        builder.fn()
        return builder.build()
    }
}

class DefaultRequestBuilder() : RequestBuilder {

    constructor(other: Request) : this() {
        this.path = other.path
        this.pathParameters.putAll(other.pathParameters)
        this.method = other.method
    }

    private var path = ""
    private var method = Method.GET
    private val pathParameters: MutableMap<String, String> = hashMapOf()

    override fun path(path: String): RequestBuilder {
        this.path = path
        return this
    }

    override fun method(method: Method): RequestBuilder {
        this.method = method
        return this
    }

    override fun pathParameter(name: String, value: String): RequestBuilder {
        pathParameters.put(name, value)
        return this
    }

    override fun build(): Request {
        return BaseRequest(
                path = this.path,
                method = this.method,
                pathParameters = this.pathParameters
        )
    }
}

inline fun request(fn: RequestBuilder.() -> Unit): Request {
    val builder = DefaultRequestBuilder()
    builder.fn()
    return builder.build()
}
