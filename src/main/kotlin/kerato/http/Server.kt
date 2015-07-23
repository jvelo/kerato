package http

import http.routes.RequestResponseLambdaRoute
import http.routes.Route
import http.routes.DefaultRoutesBuilder
import org.glassfish.grizzly.http.server.HttpHandler
import org.glassfish.grizzly.http.server.HttpServer
import org.json.JSONObject
import org.glassfish.grizzly.http.server.Request as GrizzlyRequest
import org.glassfish.grizzly.http.server.Response as GrizzlyResponse
import org.slf4j.LoggerFactory
import java.io.Writer
import java.lang.reflect.Constructor
import java.text.SimpleDateFormat
import java.util
import java.util.*
import kotlin.reflect.KCallable

/**
 * @version $Id$
 */
class Server() {

    private val logger = LoggerFactory.getLogger(::Server.javaClass)

    private var httpServer: HttpServer? = null

    private val routes: MutableList<Route> = arrayListOf()

    private var port: Int = 8080

    public fun routes(fn: DefaultRoutesBuilder.() -> Unit) {
        val routes = DefaultRoutesBuilder()
        routes.fn()
        this.routes.addAll(routes.all())
    }

    public inline fun configure(fn: Server.() -> Unit): Server {
        this.fn()
        return this
    }

    public fun port(port: Int) {
        this.port = port
    }

    public fun start() {
        this.build()
        this.httpServer?.start()
    }

    private fun build() : Server{
        httpServer = HttpServer.createSimpleServer(".", this.port)
        httpServer?.getServerConfiguration()?.addHttpHandler(object : HttpHandler() {
            override fun service(grizzlyRequest: GrizzlyRequest, grizzlyResponse: GrizzlyResponse) {

                val request = request {
                    path(grizzlyRequest.getHttpHandlerPath())
                    method(Method.valueOf(grizzlyRequest.getMethod().getMethodString()))
                }
                val initialResponse = response {
                    status(Status.NOT_FOUND)
                }

                val matchingRoutes = routes.filter { it.matches(request) }

                val exchange = matchingRoutes.fold(Exchange(request, initialResponse), {
                    exchange, route -> when (exchange.response.halted) {
                        true -> exchange
                        else -> route.apply(exchange)
                    }
                })

                grizzlyResponse.setStatus(exchange.response.status)
                exchange.response.headers.forEach { entry ->
                    grizzlyResponse.addHeader(entry.key, entry.value)
                }

                when (exchange.response.type()) {
                    "application/json" -> {
                        val payload: JSONObject = when (exchange.response.body) {
                            // Assume maps are always maps of String, Any
                            is Map<*, *> -> JSONObject(exchange.response.body as Map<String, Any>)

                            else -> JSONObject(exchange.response.body)
                        }
                        grizzlyResponse.getWriter().write(payload.toString())
                        grizzlyResponse.addHeader("Content-Type", "application/json; charset=UTF-8")
                    }
                    else -> {
                        grizzlyResponse.getWriter().write(exchange.response.body.toString())
                        grizzlyResponse.addHeader("Content-Type", "text/plain; charset=UTF-8")
                    }
                }

            }
        }, "/")
        logger.info("Server started with port {}", this.port)
        return this
    }
}