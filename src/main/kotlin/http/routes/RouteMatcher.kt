package http.routes

import http.Request

/**
 * @version $Id$
 */
public class RouteMatcher {

    fun matches(request: Request, route: Route) : Boolean {
        val methodMatches = route.methods.contains(request.method)
        val pathMatches = route.uri.equals(request.path)

        return methodMatches && pathMatches
    }

}