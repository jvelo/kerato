package http

import org.glassfish.grizzly.http.server.HttpHandler
import org.glassfish.grizzly.http.server.HttpServer
import org.glassfish.grizzly.http.server.Request as GrizzlyRequest
import org.glassfish.grizzly.http.server.Response as GrizzlyResponse
import org.slf4j.LoggerFactory
import java.text.SimpleDateFormat
import java.util.*

/**
 * @version $Id$
 */
class Server(val handler: (request: Request, response:Response) -> Response) {

    private val logger = LoggerFactory.getLogger(::Server.javaClass);

    private val httpServer: HttpServer

    init {
        logger.info("Starting server...")
        httpServer = HttpServer.createSimpleServer()
        httpServer.getServerConfiguration().addHttpHandler(object : HttpHandler() {
            override fun service(grizzlyRequest: GrizzlyRequest, grizzlyResponse: GrizzlyResponse) {

                val request = request {
                    path(grizzlyRequest.getHttpHandlerPath())
                }

                val response = handler(request, response {})

                grizzlyResponse.setStatus(response.status)
                response.headers.forEach { entry ->
                    grizzlyResponse.addHeader(entry.getKey(), entry.getValue())
                }
            }
        });
    }

    public fun start() {
        this.httpServer.start()
    }
}