package http

import org.jetbrains.spek.api.Spek
import kotlin.test.assertEquals

import com.jayway.restassured.RestAssured.given
import com.jayway.restassured.RestAssured.with
import com.jayway.restassured.RestAssured.`when`
import com.jayway.restassured.RestAssured.get
import com.jayway.restassured.http.ContentType.JSON

import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.nullValue

import java.util.*

/**
 * @version $Id$
 */
class ServerSpecs : Spek() {

    fun randomPort(): Int {
        return 1000 + Random().nextInt(8999)
    }

    init {
        /*
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
                it("should have both headers and the last matched body") {
                    given().port(port)
                            .expect().body(equalTo("Matched second"))
                            .expect().header("X-Match-First", "Yes")
                            .expect().header("X-Match-Second", "Also yes")
                            .`when`().get("/foo")
                }
            }
        }

        given("a server with several routes matching the same path, the first route halting") {
            val server = Server()
            val port = randomPort()
            server.configure {
                port(port)
                routes {
                    get("/foo", { request, response ->
                        response.halt {
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
                it("should have only the headers and body from the first route") {
                    given().port(port)
                            .expect().body(equalTo("Matched first"))
                            .expect().header("X-Match-First", "Yes")
                            .expect().header("X-Match-Second", `is`(nullValue()))
                            .`when`().get("/foo")
                }
            }
        }*/

        // JSON ------------------------------------------------------------------------------------

        given("a server with a route that returns json") {
            val server = Server()
            val port = randomPort()
            server.configure {
                port(port)
                routes {
                    get("/foo", { request, response ->
                        response.with {
                            json(mapOf("hello" to "world"))
                        }
                    })
                }
            }
            server.start()

            on("hitting that route") {
                it("should have correct payload and content type") {
                    given().port(port)
                            .expect().body("hello", equalTo("world"))
                            .expect().contentType(JSON)
                            .`when`().get("/foo")
                }
            }
        }

        given("a server with a route that returns json with one property via vararg") {
            val server = Server()
            val port = randomPort()
            server.configure {
                port(port)
                routes {
                    get("/foo", { request, response ->
                        response.with {
                            json("hello" to "world")
                        }
                    })
                }
            }
            server.start()

            on("hitting that route") {
                it("should have correct payload and content type") {
                    given().port(port)
                            .expect().body("hello", equalTo("world"))
                            .expect().contentType(JSON)
                            .`when`().get("/foo")
                }
            }
        }

        given("a server with a route that returns json with several property via vararg") {
            val server = Server()
            val port = randomPort()
            server.configure {
                port(port)
                routes {
                    get("/foo", { request, response ->
                        response.with {
                            json(
                                "hello" to "world",
                                "hallo" to "welt"
                            )
                        }
                    })
                }
            }
            server.start()

            on("hitting that route") {
                it("should have correct payload and content type") {
                    given().port(port)
                            .expect().body("hello", equalTo("world"))
                            .expect().body("hallo", equalTo("welt"))
                            .expect().contentType(JSON)
                            .`when`().get("/foo")
                }
            }
        }
    }
}
