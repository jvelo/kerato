package http.routes

import http.*
import org.jetbrains.spek.api.Spek
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.expect

/**
 * @version $Id$
 */
class RoutesSpecs : Spek() {

    init {
        given("a route collection") {
            val routes = DefaultRoutesBuilder()

            on("adding routes with the builder/fluent API") {
                routes
                        .get("/something", { request, response -> response { body("Yes, something") }})
                        .get("/other", { request, response ->
                            ok()
                        })
                        .get("/ping", { request, response -> ok() })
                        .get("/foo/:name/", { request, response -> seeOther("/other") })

                it("should add the routes") {
                    assertEquals(4, routes.all().size())
                }
            }

        }

        given("a route builder") {
            on("adding routes via the DSL API") {

                val routes = routes {
                    get("/customer/:id", { request, response -> ok() })
                    get("/customer/:id/balance", { request, response -> ok() })
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

            on("nesting routes definition in at") {
                val routes = routes {
                    at("/customer", {
                        get("/:id", { request, response -> ok() })
                        get("/:id/balance", { request, response -> ok() })
                    })
                }

                it("should add the routes") {
                    assertEquals(2, routes.all().size())
                    assertNotNull(routes.all().firstOrNull {
                        it.path == "/customer/:id"
                    })
                }

                val otherRoutes = routes {
                    at("customer", {
                        get(":id", { request, response -> ok() })
                        get(":id/balance", { request, response -> ok() })
                    })
                }

                it("should add the routes, adding the leading slashes as necessary") {
                    assertEquals(2, otherRoutes.all().size())
                    assertNotNull(otherRoutes.all().firstOrNull {
                        it.path == "/customer/:id"
                    })
                }
            }
        }
    }
}