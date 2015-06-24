package http.routes

import http.Method

/**
 * @version $Id$
 */
public open data class Route(
        val methods: Array<Method>,
        val uri: String
) {
    constructor(method: Method, uri: String) :
    this(arrayOf(method), uri)
}