package kerato.http.routes

import kerato.http.*
import java.lang.reflect.Method

public class InvokableHandler(
        val method: Method,
        val handler: Any
) : (Exchange) -> Exchange {

    override fun invoke(exchange: Exchange): Exchange {
        val arguments = argumentsForMethodCall(exchange.request, exchange.response, this.method)
        val result = this.method.invoke(handler, *arguments)
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

        return Exchange(exchange.request, response)
    }

    private fun argumentsForMethodCall(request: Request, response: Response, javaMethod: Method): Array<Any?> {
        return javaMethod.getParameterTypes().map({
            when {
                javaClass<Request>().isAssignableFrom(it) -> request
                javaClass<Response>().isAssignableFrom(it) -> response
                else -> null;
            }
        }).toTypedArray()
    }
}