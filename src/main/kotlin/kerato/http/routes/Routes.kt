package kerato.http.routes

import kerato.http.HttpMethod
import kerato.http.Request
import kerato.http.Response
import java.lang.reflect.Method
import kotlin.text.Regex

public interface Routes {
    fun all(): List<RouteEntry>
}

public interface RoutesBuilder {
    fun at(path: String, fn: RoutesBuilder.() -> Unit): RoutesBuilder
    fun at(path: String, controller: Any): RoutesBuilder
    fun get(path: String, handler: (request: Request, response: Response) -> Response): RoutesBuilder
    fun post(path: String, handler: (request: Request, response: Response) -> Response): RoutesBuilder

    fun build(): Routes
}

public class DefaultRoutes(val routes: List<RouteEntry>) : Routes {
    override fun all(): List<RouteEntry> {
        return routes
    }
}

fun String.asPathSegment(): String = when {
    this == "" -> this
    this.endsWith("/") -> this
    else -> this.concat("/")
}

fun withPathParameters(path: String, request: Request): Request {
    val pathParts = path.split('/').filter(String::isNotEmpty)
    val requestParts = request.path.split('/').filter(String::isNotEmpty)

    val params = pathParts.mapIndexed { index, part ->
        if (part.matches(Regex("\\{.*\\}")) && index < requestParts.size()) {
            Pair(part.substring(1, part.length() - 1), requestParts.get(index))
        } else {
            null
        }
    }.filterNotNull()

    if (params.isEmpty()) return request

    return request.with {
        params.forEach { pathParameter(it.first, it.second) }
    }
}

data class InvokableMethod(
        val method: HttpMethod,
        val javaMethod: Method,
        val path: String
)

/**
 * @version $Id$
 */
public class DefaultRoutesBuilder(val path: String? = null) : RoutesBuilder {

    private val routes: MutableList<RouteEntry> = arrayListOf()

    fun all(): List<RouteEntry> {
        return this.routes
    }

    override final inline fun at(path: String, fn: RoutesBuilder.() -> Unit): RoutesBuilder {
        val routes = DefaultRoutesBuilder(path)
        routes.fn()
        this.routes.addAll(routes.build().all())
        return this
    }

    override fun at(path: String, controller: Any): RoutesBuilder {

        val controllerPathPrefix = controller.javaClass.getAnnotationsByType(javaClass<at>()).firstOrNull()?.let { it.path } ?: ""
        val pathPrefix = this.path.orEmpty().asPathSegment()
                .concat(path.asPathSegment())
                .concat(controllerPathPrefix.asPathSegment())

        val controllerMethods: List<InvokableMethod> = controller.javaClass.getMethods().map { method ->
            // First, try to find the HTTP method by name : when the name of the java method matches
            // the HTTP method lowercase, we consider it's the intended method
            HttpMethod.values().firstOrNull { it.name().toLowerCase().equals(method.getName()) }?.let {
                buildMethod(it, method, prefix = pathPrefix)
            } ?: method.getAnnotations().map {
                // If not found, let's look at the method annotations
                when (it) {
                    is get -> buildMethod(HttpMethod.GET, method, it, pathPrefix)
                    is post -> buildMethod(HttpMethod.POST, method, it, pathPrefix)
                    is delete -> buildMethod(HttpMethod.DELETE, method, it, pathPrefix)
                    is put -> buildMethod(HttpMethod.PUT, method, it, pathPrefix)
                    is options -> buildMethod(HttpMethod.OPTIONS, method, it, pathPrefix)
                    is patch -> buildMethod(HttpMethod.PATCH, method, it, pathPrefix)
                    else -> null
                }
            }.firstOrNull()
        }.filterNotNull()

        controllerMethods.forEach {
            routes.add(RouteEntry(
                    method = it.method,
                    path = it.path,
                    handler = InvokableHandler(it.javaMethod, controller)
            ))
        }

        return this
    }

    override fun get(path: String, handler: (request: Request, response: Response) -> Response): RoutesBuilder {
        return this.add(HttpMethod.GET, pathFor(path), handler)
    }

    override fun post(path: String, handler: (request: Request, response: Response) -> Response): RoutesBuilder {
        return this.add(HttpMethod.POST, pathFor(path), handler)
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Handler takes a request and existing response, returns a response (possibly modified)
     */
    private fun add(method: HttpMethod, path: String, handler: (request: Request, response: Response) -> Response): RoutesBuilder {
        routes.add(RouteEntry(method, path, handler = RequestResponseLambdaHandler(handler)))
        return this
    }

    private fun ensureLeadingSlashWhenNotEmpty(path: String): String = when {
        path.equals("") -> path
        path.startsWith("/") -> path
        else -> "/${path}"
    }

    private fun pathFor(path: String): String {
        return ensureLeadingSlashWhenNotEmpty(this.path.orEmpty())
                .concat(ensureLeadingSlashWhenNotEmpty(path))
    }

    private fun buildMethod(
            method: HttpMethod,
            javaMethod: Method,
            annotation: Annotation? = null,
            prefix: String? = null
    ): InvokableMethod {
        val pathField: Method? = try {
            annotation?.javaClass?.getDeclaredMethod("path")
        } catch (e: NoSuchFieldException) {
            null
        }

        val path = pathField?.invoke(annotation) as String?
        return InvokableMethod(method, javaMethod, prefix.orEmpty().concat(path.orEmpty()))
    }

    override fun build(): Routes {
        return DefaultRoutes(routes)
    }

}

inline fun routes(fn: RoutesBuilder.() -> Unit): Routes {
    val routes = DefaultRoutesBuilder()
    routes.fn()
    return routes.build()
}