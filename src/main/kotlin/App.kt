package cool.naze

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.MetricRegistry.name
import http.Server
import http.Status
import http.ok
import http.response


object app {

    private val server: Server = Server()

    private val logger = LoggerFactory.getLogger(::main.javaClass);

    private final val metrics = MetricRegistry();

    fun start(): Unit {
        Runtime.getRuntime().addShutdownHook(Thread("shutdown"))

        val start = metrics.timer(name(javaClass<app>(), "server"))
        val context = start.time()

        try {
            server.configure {
                routes {
                    get("/foo", { request, response -> response {
                        status(Status.OK)
                        body("Yes : ${request.path}")
                    }})
                }
            }
            server.build().start()
            val time = context.stop()
            logger.info("Server started in {} ms", TimeUnit.MILLISECONDS.convert(time, TimeUnit.NANOSECONDS))
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

