package http.routes

import http.*
import org.jetbrains.spek.api.Spek
import kotlin.test.assertEquals
import kotlin.test.expect

/**
 * @version $Id$
 */
class RoutesSpecs : Spek() {

    init {
        given("a route collection") {

            val routes = Routes()

            on("adding routes with the builder/fluent API") {
                routes
                        .get("/something", { -> "Yes, something" })
                        .get("/other", { request, response ->
                            ok()
                        })
                        .get("/ping", { request -> ok() })
                        .get("/foo/:name/", { -> seeOther("/other") })

                it("should add the routes") {
                    assertEquals(4, routes.all().size())
                }
            }

        }

        given("a route builder") {
            on("adding routes via the DSL API") {

                val routes = routes {
                    get("/customer/:id", { request -> ok() })
                    get("/customer/:id/balance", { request -> ok() })
                }

                it("should add the routes") {
                    assertEquals(2, routes.all().size())
                }
            }

            on("adding controller route as object instance") {
                val controller = object {
                    get fun doGet() {
                        ok()
                    }
                }
                val routes = routes {
                    at("/customer", controller)
                }

                it("should add the routes") {
                    assertEquals(1, routes.all().size())
                }
            }
        }
    }
}