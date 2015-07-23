package http.routes

import http.Exchange
import http.Method
import http.Request

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