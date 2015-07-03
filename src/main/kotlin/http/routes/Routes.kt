package http.routes

import http.Method
import http.Request
import http.Response
import http.ok

/**
 * @version $Id$
 */
public class Routes {

    private val routes: MutableList<Route> = arrayListOf()

    fun all(): List<Route> {
        return this.routes
    }

    fun at(pattern: String, controller: Any) : Routes {
        routes.add(ControllerRoute(Method.values(), pattern, controller))
        return this
    }

    fun get(pattern: String, handler: () -> Any) : Routes {
        return this.add(Method.GET, pattern, handler)
    }

    fun get(pattern: String, handler: (request: Request) -> Response) : Routes {
        return this.add(Method.GET, pattern, handler)
    }

    fun get(pattern: String, handler: (request: Request, response: Response) -> Response) : Routes {
        return this.add(Method.GET, pattern, handler)
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Handler just returns a payload object (200 OK assumed)
     */
    private fun add(method: Method, pattern: String, handler: () -> Any): Routes {
        //routes.add(Route(arrayOf(method), pattern, handler))
        routes.add(RequestResponseLambdaRoute(Method.values(), pattern, { (req, resp) -> ok() }))
        return this
    }

    /**
     * Handler takes a request, returns a response
     */
    private fun add(method: Method, pattern: String, handler: (request: Request) -> Response): Routes {
        //routes.add(Route(arrayOf(method), pattern, handler))
        routes.add(RequestResponseLambdaRoute(Method.values(), pattern, { (req, resp) -> ok() }))
        return this
    }

    /**
     * Handler takes a request and existing response, returns a response (possibly modified)
     */
    private fun add(method: Method, pattern: String, handler: (request: Request, response: Response) -> Response): Routes {
        routes.add(RequestResponseLambdaRoute(arrayOf(method), pattern, handler))
        return this
    }

}

inline fun routes(fn: Routes.() -> Unit): Routes {
    val routes = Routes()
    routes.fn()
    return routes
}