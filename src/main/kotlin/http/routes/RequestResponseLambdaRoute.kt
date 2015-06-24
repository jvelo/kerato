package http.routes

import http.Method
import http.Request
import http.Response

/**
 * @version $Id$
 */
public class RequestResponseLambdaRoute(
        methods: Array<Method>,
        uri: String,
        val handler: (request: Request, response: Response) -> Response) : Route(methods, uri) {

    constructor(method: Method, uri: String, handler: (request: Request, response: Response) -> Response) :
    this(arrayOf(method), uri, handler)
}