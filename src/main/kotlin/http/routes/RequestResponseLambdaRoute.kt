package http.routes

import http.*
import kotlin.text.Regex

/**
 * @version $Id$
 */
public class RequestResponseLambdaRoute(
        methods: Array<Method>,
        uri: String,
        val handler: (request: Request, response: Response) -> Response) : Route(methods, uri) {

    override fun matches(request: Request): Boolean {
        val methodMatches = this.methods.contains(request.method)
        val pathMatches = this.pathMatches(request)

        return methodMatches && pathMatches
    }

    constructor(method: Method, uri: String, handler: (request: Request, response: Response) -> Response) :
    this(arrayOf(method), uri, handler)

    override fun apply(exchange: Exchange): Exchange {

        val request = extractPathParams(exchange.request)

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

    private fun extractPathParams(request: Request): Request {
        val routeParts = this.path.split('/').filter(String::isNotEmpty)
        val requestParts = request.path.split('/').filter(String::isNotEmpty)

        val params = routeParts.mapIndexed { index, part ->
            if (part.matches(Regex("\\{.*\\}")) && index < requestParts.size()) {
                Pair(part.substring(1, part.length() - 1), requestParts.get(index))
            } else {
                null
            }
        }.filterNotNull() : List<Pair<String, String>>

        if (params.isEmpty()) return request

        return request.with {
            params.forEach { pathParameter(it.first, it.second) }
        }
    }

    fun pathMatches(request: Request): Boolean {
        val routeParts = this.path.split('/').filter(String::isNotEmpty)
        val requestParts = request.path.split('/').filter(String::isNotEmpty)

        val partsMatching = routeParts.mapIndexed { index, part ->
            when {
                part.matches(Regex("\\{.*\\}")) -> true
                requestParts.size() < index -> false
                part.equals(requestParts[index]) -> true
                else -> false
            }
        }

        return partsMatching.none { it == false }
    }
}