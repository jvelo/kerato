package kerato.http

import java.io.*
import java.net.InetSocketAddress
import kotlin.reflect.KClass

/**
 * @version $Id$
 */

public interface Request {
    val path: String
    val method: Method
    val pathParameters: Map<String, String>
    val clientAddress: InetSocketAddress
    val payload: InputStream?

    val content: String?
        get() {
            val reader = this.payload?.bufferedReader() ?: return null
            try {
                return reader.readText()
            } finally {
                reader.close()
            }
        }

    fun pathParameter(name: String) : String?

    final inline fun <reified T> pathParameterAs(name: String): T? {
        val value = pathParameter(name)

        when (value) {
            null -> return null
            is T -> return value
        }

        val javaClass = javaClass<T>()
        val valueOfMethod = try { javaClass.getMethod("valueOf", javaClass<String>()) } catch (e: NoSuchMethodException) { null }
        val stringConstructor = try { javaClass.getConstructor(javaClass<String>()) } catch (e: NoSuchMethodException) { null }

        return when {
            valueOfMethod != null -> valueOfMethod.invoke(null, value) as T
            stringConstructor != null -> stringConstructor.newInstance(value)
            else -> null
        }
    }

    final inline fun with(fn: RequestBuilder.() -> Unit): Request {
        val builder = DefaultRequestBuilder(this)
        builder.fn()
        return builder.build()
    }
}

public interface RequestBuilder {
    fun path(path: String): RequestBuilder
    fun method(method: Method): RequestBuilder
    fun pathParameter(name: String, value: String): RequestBuilder
    fun payload(payload: InputStream): RequestBuilder
    fun clientAddress(clientAddress: InetSocketAddress): RequestBuilder

    fun build(): Request
}

public data class DefaultRequest(
        override val path: String,
        override val method: Method,
        override val pathParameters: Map<String, String>,
        override val clientAddress: InetSocketAddress,
        override val payload: InputStream?
) : Request  {

    override fun pathParameter(name: String) = pathParameters.get(name)
}

class DefaultRequestBuilder() : RequestBuilder {

    constructor(other: Request) : this() {
        this.path = other.path
        this.pathParameters.putAll(other.pathParameters)
        this.method = other.method
        this.payload = other.payload
        this.clientAddress = other.clientAddress
    }

    private var clientAddress = InetSocketAddress.createUnresolved("0.0.0.0", 80);
    private var path = ""
    private var method = Method.GET
    private val pathParameters: MutableMap<String, String> = hashMapOf()
    private var payload: InputStream? = null

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

    override fun payload(payload: InputStream): RequestBuilder {
        this.payload = payload
        return this
    }

    override fun clientAddress(clientAddress: InetSocketAddress): RequestBuilder {
        this.clientAddress = clientAddress
        return this
    }

    override fun build(): Request {
        return DefaultRequest(
                path = this.path,
                method = this.method,
                pathParameters = this.pathParameters,
                clientAddress = this.clientAddress,
                payload = this.payload
        )
    }
}

inline fun request(fn: RequestBuilder.() -> Unit): Request {
    val builder = DefaultRequestBuilder()
    builder.fn()
    return builder.build()
}
