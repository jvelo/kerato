package kerato

import kerato.http.Server
import kerato.http.Status
import kerato.http.response
import org.slf4j.LoggerFactory
import java.io.IOException

object app {

    private val server: Server = Server()

    private val logger = LoggerFactory.getLogger(::main.javaClass);

    fun start(): Unit {
        Runtime.getRuntime().addShutdownHook(Thread("shutdown"))
        try {
            server.configure {
                routes {
                    get("/order/{id}", { request, response ->
                        response.with {
                            body(request.pathParameter("id").orEmpty())
                            header("X-Witness", request.pathParameter("other").orEmpty())
                        }
                    })
                    get("/some/{float}", { request, response ->
                        response.with {
                            val transactionId = request.pathParameterAs<Float>("float")
                            body(java.lang.String.format("%.2f", transactionId ?: -1f))
                            header("X-Witness", request.pathParameter("other").orEmpty())
                        }
                    })
                }
            }
            server.start()
            logger.info("Server started")
            logger.info("CTRL^C to exit..")
            Thread.currentThread().join()
        } catch (e: IOException) {
            logger.error("Failed to start server", e)
        } catch (e: InterruptedException) {
            logger.error("Failed to start server", e)
        }
    }
}

fun main(args: Array<String>) {
    app.start();
}

