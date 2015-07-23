package http.routes

import http.Request
import kotlin.text.Regex

/**
 * @version $Id$
 */
public interface PathHandler {

    fun pathMatches(path: String, request: Request): Boolean {
        val routeParts = path.split('/').filter(String::isNotEmpty)
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

    fun withPathParameters(path: String, request: Request): Request {
        val pathParts = path.split('/').filter(String::isNotEmpty)
        val requestParts = request.path.split('/').filter(String::isNotEmpty)

        val params = pathParts.mapIndexed { index, part ->
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
}