package kerato.http.routes

import kerato.http.Exchange
import kerato.http.HttpMethod
import kerato.http.Request
import kerato.http.request
import kotlin.text.Regex

/**
 * @version $Id$
 */
public data class RouteEntry(
        val method: HttpMethod = HttpMethod.GET,
        val path: String = "{*}",
        val acceptsType: String = "*/*",
        val providesType: String = "*/*",
        val handler: (Exchange) -> Exchange = { it } // noop
) {
    fun matches(request: Request): Boolean =
            pathMatches(request) && methodMatches(request)

    fun methodMatches(request: Request): Boolean {
        return when (request.method) {
            HttpMethod.HEAD -> this.method.equals(HttpMethod.HEAD) || this.method.equals(HttpMethod.GET)
            else -> this.method.equals(request.method)
        }
    }

    fun pathMatches(request: Request): Boolean {
        val routeParts = this.path.split('/').filter(String::isNotEmpty)
        val requestParts = request.path.split('/').filter(String::isNotEmpty)

        if (requestParts.size() != routeParts.size()) {
            return false;
        }

        val partsMatching = routeParts.mapIndexed { index, part ->
            when {
                part.matches(Regex("\\{.*\\}")) -> true
                part.equals(requestParts[index]) -> true
                else -> false
            }
        }

        return partsMatching.none { it == false }
    }
}