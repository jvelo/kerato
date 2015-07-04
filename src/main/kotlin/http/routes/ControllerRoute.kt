package http.routes

import http.*
import java.lang.reflect.Method as JavaMethod

/**
 * @version $Id$
 */
public class ControllerRoute(
        methods: Array<Method>,
        uri: String,
        val handler: Any
): Route(methods, uri) {

    val funs : List<Pair<Method, JavaMethod>>

    init {
        funs = handler.javaClass.getMethods().map { method ->
            method.getAnnotations().map {
                when (it) {
                    is get -> Pair(Method.GET, method)
                    is post -> Pair(Method.POST, method)
                    is delete -> Pair(Method.DELETE, method)
                    is put -> Pair(Method.PUT, method)
                    is options -> Pair(Method.OPTIONS, method)
                    is patch -> Pair(Method.PATCH, method)
                    else -> null
                }
            }.firstOrNull()
        }.filterNotNull()
    }

    override fun matches(request: Request): Boolean {
        return this.path.equals(request.path) && funs.any {
            request.method == it.first
        }
    }

    override fun apply(exchange: Exchange): Exchange {
        val method = funs.firstOrNull { exchange.request.method == it.first } ?: return exchange

        val arguments = argumentsForMethodCall(exchange.request, method.second)
        val result = method.second.invoke(handler, *arguments)
        val response : Response = when (result) {
            is CopiedResponse -> result
            is BaseResponse -> response {
                merge(exchange.response)
                merge(result)
            }
            is Unit -> exchange.response
            else -> exchange.response.with {
                body(result)
            }
        }

        return Exchange(exchange.request, response)
    }

    private fun argumentsForMethodCall(request: Request, javaMethod: JavaMethod) : Array<Any> {
        return arrayOf()
    }

}