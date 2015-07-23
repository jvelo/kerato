package kerato.http.routes

import kerato.http.Exchange
import kerato.http.Method
import kerato.http.Request

/**
 * @version $Id$
 */
public abstract data class Route(
        val methods: Array<Method>,
        val path: String
) {
    constructor(method: Method, uri: String) :
    this(arrayOf(method), uri)

    abstract fun apply(exchange: Exchange) : Exchange

    abstract fun matches(request: Request) : Boolean
}