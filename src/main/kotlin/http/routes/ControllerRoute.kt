package http.routes

import http.*
import java.lang.reflect.Field
import java.lang.reflect.Method as JavaMethod

/**
 * @version $Id$
 */
public class ControllerRoute(
        methods: Array<Method>,
        path: String,
        val handler: Any
) : Route(methods, path), PathHandler {

    data class ControllerMethod(
            val method: Method,
            val javaMethod: JavaMethod,
            val path: String = ""
    )

    val funs: List<ControllerMethod>
    val pathPrefix: String

    init {
        pathPrefix = ensureEndsWithSlash(this.path) +
                ensureEndsWithSlash(handler.javaClass.getAnnotationsByType(javaClass<at>())
                        .map { it.path }.firstOrNull() ?: ""
                )

        funs = handler.javaClass.getMethods().map { method ->
            method.getAnnotations().map {
                when (it) {
                    is get -> buildMethod(Method.GET, method, it)
                    is post -> buildMethod(Method.POST, method, it)
                    is delete -> buildMethod(Method.DELETE, method, it)
                    is put -> buildMethod(Method.PUT, method, it)
                    is options -> buildMethod(Method.OPTIONS, method, it)
                    is patch -> buildMethod(Method.PATCH, method, it)
                    else -> null
                }
            }.firstOrNull()
        }.filterNotNull()
    }

    private fun ensureEndsWithSlash(path: String): String = when {
        path == "" -> path
        path.endsWith("/") -> path
        else -> path.concat("/")
    }

    private fun buildMethod(method: Method, javaMethod: JavaMethod, annotation: Annotation): ControllerMethod {
        val pathField: JavaMethod? = try {
            annotation.javaClass.getDeclaredMethod("path")
        } catch (e: NoSuchFieldException) {
            null
        }

        val path = pathField?.invoke(annotation) as String?
        val methodPath = pathPrefix.concat(path.orEmpty())
        return ControllerMethod(method, javaMethod, methodPath)
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
            null -> exchange.response
            is Unit -> exchange.response
            else -> exchange.response.with {
                body(result)
            }
        }

        return Exchange(request, response)
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