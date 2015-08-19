package kerato.http.routes

import kerato.http.*
import java.lang.reflect.Method as JavaMethod

fun String.asPathSegment(): String = when {
    this == "" -> this
    this.endsWith("/") -> this
    else -> this.concat("/")
}

public class ControllerRoute(
        path: String,
        val handler: Any
) : Route(HttpMethod.values(), path), PathHandler {

    data class ControllerMethod(
            val method: HttpMethod,
            val javaMethod: JavaMethod,
            val path: String = ""
    )

    val funs: List<ControllerMethod>
    val pathPrefix: String

    init {
        pathPrefix = this.path.asPathSegment() +
                this.getControllerAnnotatedPath().asPathSegment()

        funs = handler.javaClass.getMethods().map { method ->
            // First, try to find the method by name
            HttpMethod.values().firstOrNull { it.name().toLowerCase().equals(method.getName()) }?.let {
                buildMethod(it, method)
            } ?: method.getAnnotations().map {
                // If not found, let's look at the method annotations
                when (it) {
                    is get -> buildMethod(HttpMethod.GET, method, it)
                    is post -> buildMethod(HttpMethod.POST, method, it)
                    is delete -> buildMethod(HttpMethod.DELETE, method, it)
                    is put -> buildMethod(HttpMethod.PUT, method, it)
                    is options -> buildMethod(HttpMethod.OPTIONS, method, it)
                    is patch -> buildMethod(HttpMethod.PATCH, method, it)
                    else -> null
                }
            }.firstOrNull()
        }.filterNotNull()
    }

    override fun matches(request: Request): Boolean = funs.any {
        request.method == it.method && pathMatches(it.path, request)
    }

    override fun apply(exchange: Exchange): Exchange {
        val method = funs.firstOrNull { exchange.request.method == it.method } ?: return exchange

        val request = withPathParameters(method.path, exchange.request)

        val arguments = argumentsForMethodCall(request, exchange.response, method.javaMethod)
        val result = method.javaMethod.invoke(handler, *arguments)
        val response: Response = when (result) {
            is CopiedResponse -> result
            is BaseResponse -> response {
                merge(exchange.response)
                merge(result)
            }
            is Unit -> exchange.response
            null -> exchange.response
            else -> exchange.response.with {
                body(result)
            }
        }

        return Exchange(request, response)
    }

    private fun getControllerAnnotatedPath(): String =
            this.handler.javaClass.getAnnotationsByType(javaClass<at>()).firstOrNull()?.let { it.path } ?: ""

    private fun buildMethod(method: HttpMethod, javaMethod: JavaMethod, annotation: Annotation? = null): ControllerMethod {
        val pathField: JavaMethod? = try {
            annotation?.javaClass?.getDeclaredMethod("path")
        } catch (e: NoSuchFieldException) {
            null
        }

        val path = pathField?.invoke(annotation) as String?
        val methodPath = pathPrefix.concat(path.orEmpty())
        return ControllerMethod(method, javaMethod, methodPath)
    }

    private fun argumentsForMethodCall(request: Request, response: Response, javaMethod: JavaMethod): Array<Any?> {
        return javaMethod.getParameterTypes().map({
            when {
                it.isAssignableFrom(javaClass<Request>()) -> request
                it.isAssignableFrom(javaClass<Response>()) -> response
                else -> null;
            }
        }).toTypedArray()
    }
}