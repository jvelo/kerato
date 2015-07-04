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

    inline fun at(path: String, fn: Routes.() -> Unit): Routes {
        this.fn()
        return this
    }

    fun at(path: String, controller: Any) : Routes {
        routes.add(ControllerRoute(Method.values(), path, controller))
        return this
    }

    fun get(path: String, handler: (request: Request, response: Response) -> Response) : Routes {
        return this.add(Method.GET, path, handler)
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Handler takes a request and existing response, returns a response (possibly modified)
     */
    private fun add(method: Method, pattern: String, path: (request: Request, response: Response) -> Response): Routes {
        routes.add(RequestResponseLambdaRoute(arrayOf(method), pattern, path))
        return this
    }

}

inline fun routes(fn: Routes.() -> Unit): Routes {
    val routes = Routes()
    routes.fn()
    return routes
}