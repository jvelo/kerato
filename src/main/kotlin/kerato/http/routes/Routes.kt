package kerato.http.routes

import kerato.http.Method
import kerato.http.Request
import kerato.http.Response

public interface Routes {
    fun all(): List<Route>
}

public interface RoutesBuilder {
    fun at(path: String, fn: RoutesBuilder.() -> Unit): RoutesBuilder
    fun at(path: String, controller: Any): RoutesBuilder
    fun get(path: String, handler: (request: Request, response: Response) -> Response): RoutesBuilder

    fun build(): Routes
}

public class DefaultRoutes(val routes: List<Route>) : Routes {
    override fun all(): List<Route> {
        return routes
    }
}

/**
 * @version $Id$
 */
public class DefaultRoutesBuilder(val path: String? = null) : RoutesBuilder {

    private val routes: MutableList<Route> = arrayListOf()

    fun all(): List<Route> {
        return this.routes
    }

    override final inline fun at(path: String, fn: RoutesBuilder.() -> Unit): RoutesBuilder {
        val routes = DefaultRoutesBuilder(path)
        routes.fn()
        this.routes.addAll(routes.build().all())
        return this
    }

    override fun at(path: String, controller: Any): RoutesBuilder {
        routes.add(ControllerRoute(pathFor(path), controller))
        return this
    }

    override fun get(path: String, handler: (request: Request, response: Response) -> Response): RoutesBuilder {
        return this.add(Method.GET, pathFor(path), handler)
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Handler takes a request and existing response, returns a response (possibly modified)
     */
    private fun add(method: Method, pattern: String, path: (request: Request, response: Response) -> Response): RoutesBuilder {
        routes.add(RequestResponseLambdaRoute(arrayOf(method), pattern, path))
        return this
    }

    private fun ensureLeadingSlashWhenNotEmpty(path: String) : String = when {
        path.equals("") -> path
        path.startsWith("/") -> path
        else -> "/${path}"
    }

    private fun pathFor(path: String): String {
        return ensureLeadingSlashWhenNotEmpty(this.path.orEmpty())
                .concat(ensureLeadingSlashWhenNotEmpty(path))
    }

    override fun build(): Routes {
        return DefaultRoutes(routes)
    }

}

inline fun routes(fn: RoutesBuilder.() -> Unit): Routes {
    val routes = DefaultRoutesBuilder()
    routes.fn()
    return routes.build()
}