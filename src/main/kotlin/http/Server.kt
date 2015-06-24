package http

import http.routes.RequestResponseLambdaRoute
import http.routes.Route
import http.routes.RouteMatcher
import http.routes.Routes
import org.glassfish.grizzly.http.server.HttpHandler
import org.glassfish.grizzly.http.server.HttpServer
import org.glassfish.grizzly.http.server.Request as GrizzlyRequest
import org.glassfish.grizzly.http.server.Response as GrizzlyResponse
import org.slf4j.LoggerFactory
import java.io.Writer
import java.text.SimpleDateFormat
import java.util.*
import kotlin.reflect.KCallable


/**
 * @version $Id$
 */
class Server() {

    private val logger = LoggerFactory.getLogger(::Server.javaClass);

    private val httpServer: HttpServer

    private val routes: MutableList<Route> = arrayListOf()

    private val routeMatcher = RouteMatcher()

    public fun routes(fn: Routes.() -> Unit) {
        val routes = Routes()
        routes.fn()
        this.routes.addAll(routes.all())
    }

    public inline fun configure(fn: Server.() -> Unit): Server {
        this.fn()
        return this;
    }

    init {
        logger.info("Starting server...")
        httpServer = HttpServer.createSimpleServer()
        httpServer.getServerConfiguration().addHttpHandler(object : HttpHandler() {
            override fun service(grizzlyRequest: GrizzlyRequest, grizzlyResponse: GrizzlyResponse) {

                val request = request {
                    path(grizzlyRequest.getHttpHandlerPath())
                    method(Method.valueOf(grizzlyRequest.getMethod().getMethodString()))
                }
                val initialResponse = response {
                    status(Status.NOT_FOUND)
                }

                val matchingRoutes = routes.filter {
                    routeMatcher.matches(request, it)
                }

                val consumedExchanged = matchingRoutes.fold(Exchange(request, initialResponse), {
                    exchange, route -> route.apply(exchange)
                })

                grizzlyResponse.setStatus(consumedExchanged.response.status)
                consumedExchanged.response.headers.forEach { entry ->
                    grizzlyResponse.addHeader(entry.getKey(), entry.getValue())
                }
                grizzlyResponse.getWriter().write(consumedExchanged.response.body.toString());
                grizzlyResponse.addHeader("Content-Type", "text/plain; charset=UTF-8")
            }
        }, "/");
    }

    public fun start() {
        this.httpServer.start()
    }
}