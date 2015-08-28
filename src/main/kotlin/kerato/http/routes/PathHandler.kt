package kerato.http.routes

import kerato.http.Request
import kotlin.text.Regex

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