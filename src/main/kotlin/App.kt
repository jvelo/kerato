package cool.naze

import http.Server
import http.Status
import http.response
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
                    get("/foo", { request, response -> response {
                        status(Status.OK)
                        json(mapOf("yolo" to "swag"))
                    }})
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

