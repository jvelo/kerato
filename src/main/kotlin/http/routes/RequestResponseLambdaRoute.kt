package http.routes

import http.*
import kotlin.text.Regex

/**
 * @version $Id$
 */
public class RequestResponseLambdaRoute(
        methods: Array<Method>,
        path: String,
        val handler: (request: Request, response: Response) -> Response) : Route(methods, path), PathHandler {

    constructor(method: Method, uri: String, handler: (request: Request, response: Response) -> Response) :
    this(arrayOf(method), uri, handler)

    override fun matches(request: Request): Boolean =
            this.methods.contains(request.method) && this.pathMatches(this.path, request)

    override fun apply(exchange: Exchange): Exchange {

        val request = withPathParameters(this.path, exchange.request)

        val result = this.handler(request, exchange.response)
        val response = when (result) {
            is CopiedResponse -> result
            else -> response {
                merge(exchange.response)
                merge(result)
            }
        }
        return Exchange(request, response)
    }

}