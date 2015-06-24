package http.routes

import http.Exchange
import http.Method

/**
 * @version $Id$
 */
public abstract data class Route(
        val methods: Array<Method>,
        val uri: String
) {
    constructor(method: Method, uri: String) :
    this(arrayOf(method), uri)

    abstract fun apply(exchange: Exchange) : Exchange
}