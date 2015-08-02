package kerato.http.routes

import kerato.http.Exchange
import kerato.http.HttpMethod
import kerato.http.Request

/**
 * @version $Id$
 */
public abstract data class Route(
        val methods: Array<HttpMethod>,
        val path: String
) {
    constructor(method: HttpMethod, uri: String) :
    this(arrayOf(method), uri)

    abstract fun apply(exchange: Exchange) : Exchange

    abstract fun matches(request: Request) : Boolean
}