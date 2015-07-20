package http.server

import com.jayway.restassured.http.ContentType.JSON
import http.Response
import http.Request
import http.Status
import http.ok
import http.response
import http.routes.get
import http.routes.post
import http.server.RestTests
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.nullValue
import org.junit.Test

/**
 * @version $Id$
 */
public class BasicTests : RestTests() {

    Test fun simple_test_with_one_route() {
        routes {
            get("/foo", { request, response ->
                response.with {
                    body("Yes.")
                }
            })
        }
        expect().body(equalTo("Yes.")).`when`().get("/foo")
        expect().statusCode(404).`when`().get("/not-found")
    }

    Test fun several_routes_returning_copied_responses() {
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

        expect()
                .body(equalTo("Matched second"))
                .header("X-Match-First", "Yes")
                .header("X-Match-Second", "Also yes")
                .`when`()
                .get("/foo")
    }

    Test fun several_routes_returning_new_responses() {
        routes {
            get("/foo", { request, response ->
                http.response {
                    body("Matched first")
                    header("X-Match-First", "Yes")
                }
            })
            get("/foo", { request, response ->
                http.response {
                    body("Matched second")
                    header("X-Match-Second", "Also yes")
                }
            })
        }
        expect()
                .body(equalTo("Matched second"))
                .header("X-Match-First", "Yes")
                .header("X-Match-Second", "Also yes")
                .`when`()
                .get("/foo")
    }

    Test fun several_routes_with_one_halting() {
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

        expect()
                .body(equalTo("Matched first"))
                .header("X-Match-First", "Yes")
                .header("X-Match-Second", `is`(nullValue()))
                .`when`()
                .get("/foo")
    }

    Test fun routes_nested_in_at_group() {
        val controller = object {
            public post fun method(): Response {
                return response {
                    status(Status.CONFLICT)
                }
            }
        }
        routes {
            at("foo", {
                get("bar", { request, response ->
                    response.halt {
                        body("Matched")
                    }
                })
                at("other", controller)
            })
        }

        expect()
                .body(equalTo("Matched"))
                .`when`()
                .get("/foo/bar")

        expect()
                .statusCode(409)
                .`when`()
                .post("/foo/other")
    }

    Test fun route_with_path_params() {
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

        expect().
                body(equalTo("123")).
                header("X-Witness", "").
                `when`().
                get("/order/123")

        expect().
                body(equalTo("456")).
                header("X-Witness", "").
                `when`().
                get("/order/456/")

        expect().
                statusCode(404).
                `when`().
                get("/order/")

        expect().
                body(equalTo("3.14")).
                `when`().
                get("/some/3.1415926535")
    }

    Test fun controller_route_with_path_params() {
        routes {
            at("/controller", object {
                public get("{id}") fun doGet(request: Request): Response {
                    return response {
                        body(request.pathParameter("id").orEmpty())
                        header("X-Witness", request.pathParameter("tada").orEmpty())
                    }
                }
            })
        }

        expect().
                body(equalTo("123")).
                header("X-Witness", "").
                `when`().
                get("/controller/123")

        expect().
                body(equalTo("456")).
                header("X-Witness", "").
                `when`().
                get("/controller/456/")
    }
}