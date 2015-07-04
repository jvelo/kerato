package http.routes

import http.*

/**
 * @version $Id$
 */
public class RequestResponseLambdaRoute(
        methods: Array<Method>,
        uri: String,
        val handler: (request: Request, response: Response) -> Response) : Route(methods, uri) {

    override fun matches(request: Request): Boolean {
        val methodMatches = this.methods.contains(request.method)
        val pathMatches = this.path.equals(request.path)

        return methodMatches && pathMatches
    }

    constructor(method: Method, uri: String, handler: (request: Request, response: Response) -> Response) :
    this(arrayOf(method), uri, handler)

    override fun apply(exchange: Exchange): Exchange {
        val result = this.handler(exchange.request, exchange.response)
        val response = when (result) {
            is CopiedResponse -> result
            else -> response {
                merge(exchange.response)
                merge(result)
            }
        }
        return Exchange(exchange.request, response)
    }
}