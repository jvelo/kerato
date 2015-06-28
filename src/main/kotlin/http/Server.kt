package http

import http.routes.RequestResponseLambdaRoute
import http.routes.Route
import http.routes.RouteMatcher
import http.routes.Routes
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

    private val routeMatcher = RouteMatcher()

    private var port: Int = 8080

    public fun routes(fn: Routes.() -> Unit) {
        val routes = Routes()
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

                val matchingRoutes = routes.filter {
                    routeMatcher.matches(request, it)
                }

                val consumedExchanged = matchingRoutes.fold(Exchange(request, initialResponse), {
                    exchange, route -> when (exchange.response.halted) {
                        true -> exchange
                        else -> route.apply(exchange)
                    }
                })

                grizzlyResponse.setStatus(consumedExchanged.response.status)
                consumedExchanged.response.headers.forEach { entry ->
                    grizzlyResponse.addHeader(entry.key, entry.value)
                }

                when (consumedExchanged.response.type()) {
                    "application/json" -> {
                        val payload: JSONObject = when (consumedExchanged.response.body) {
                            is Map<*, *> -> mapToJsonObject(consumedExchanged.response.body as Map<*, *>)
                            else -> JSONObject(consumedExchanged.response.body)
                        }
                        grizzlyResponse.getWriter().write(payload.toString())
                        grizzlyResponse.addHeader("Content-Type", "application/json; charset=UTF-8")
                    }
                    else -> {
                        grizzlyResponse.getWriter().write(consumedExchanged.response.body.toString())
                        grizzlyResponse.addHeader("Content-Type", "text/plain; charset=UTF-8")
                    }
                }

            }
        }, "/")
        logger.info("Server started with port {}", this.port)
        return this
    }

    fun mapToJsonObject(map: Map<*, *>) : JSONObject {

        // Convert map to json object with org.json.JSONObject
        // Uses reflection for now as I haven't found a better way to do so :
        // https://devnet.jetbrains.com/thread/465813?tstart=0

        val javaMap = java.util.HashMap<java.lang.String, Object>()
        map.keySet().forEach {
            javaMap.put(it as java.lang.String, map.get(it) as java.lang.Object)
        }
        val javaClass = java.lang.Class.forName("org.json.JSONObject") : Class<*>
        val javaMapClass = java.lang.Class.forName("java.util.Map") : Class<*>
        val allConstructors = javaClass.getDeclaredConstructors()
        for (constructor in allConstructors) {
            val parameterType = constructor.getParameterTypes() : Array<Class<*>>
            if (parameterType.size() == 1 && parameterType.get(0).equals(javaMapClass)) {
                return constructor.newInstance(map as java.util.Map<String, Object>) as JSONObject
            }
        }
        return JSONObject(map as java.util.Map<String, Object>)
    }

}