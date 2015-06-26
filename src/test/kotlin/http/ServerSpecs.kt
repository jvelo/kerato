package http

import org.jetbrains.spek.api.Spek
import kotlin.test.assertEquals

import com.jayway.restassured.RestAssured.given
import com.jayway.restassured.RestAssured.with
import com.jayway.restassured.RestAssured.`when`
import com.jayway.restassured.RestAssured.get
import http.routes.Option
import http.routes.Priority
import org.hamcrest.Matchers.equalTo
import java.util.*

/**
 * @version $Id$
 */
class ServerSpecs : Spek() {

    fun randomPort(): Int {
        return 1000 + Random().nextInt(8999)
    }

    init {
        given("a server with a single route") {
            val server = Server()
            val port = randomPort()
            server.configure {
                port(port)
                routes {
                    get("/foo", { request, response ->
                        response.with {
                            body("Yes.")
                        }
                    })
                }
            }
            server.start()

            on("hitting that route") {
                it("should have the response defined in the route") {
                    given().port(port).expect().body(equalTo("Yes.")).`when`().get("/foo")
                }
            }

            on("hitting any other route") {
                it("should return a 404 with no content") {
                    given().port(port).expect().statusCode(404).`when`().get("/not-found")
                }
            }
        }

        given("a server with several routes matching the same path returning copied response") {
            val server = Server()
            val port = randomPort()
            server.configure {
                port(port)
                routes {
                    get("/foo", { request, response ->
                        response.with {
                            body("Matched first")
                            header("X-Match-First", "Yes")
                        }
                    })
                    get("/foo", { request, response ->
                        response.with {
                            body("Matched second")
                            header("X-Match-Second", "Also yes")
                        }
                    })
                }
            }
            server.start()

            on("hitting that route") {
                it("should have both headers and the last matched body") {
                    given().port(port)
                            .expect().body(equalTo("Matched second"))
                            .expect().header("X-Match-First", "Yes")
                            .expect().header("X-Match-Second", "Also yes")
                            .`when`().get("/foo")
                }
            }
        }

        given("a server with several routes matching the same path returning new responses") {
            val server = Server()
            val port = randomPort()
            server.configure {
                port(port)
                routes {
                    get("/foo", { request, response ->
                        response {
                            body("Matched first")
                            header("X-Match-First", "Yes")
                        }
                    })
                    get("/foo", { request, response ->
                        response {
                            body("Matched second")
                            header("X-Match-Second", "Also yes")
                        }
                    })
                }
            }
            server.start()

            on("hitting that route") {
                it("should also have both headers and the last matched body") {
                    given().port(port)
                            .expect().body(equalTo("Matched second"))
                            .expect().header("X-Match-First", "Yes")
                            .expect().header("X-Match-Second", "Also yes")
                            .`when`().get("/foo")
                }
            }
        }
    }
}
