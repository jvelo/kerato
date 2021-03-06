package kerato.http.server

import kerato.http.Response
import kerato.http.Status
import kerato.http.response
import kerato.http.routes.post
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
            get("/foo") { request, response ->
                response.with {
                    body("Yes.")
                }
            }
        }
        expect().body(equalTo("Yes.")).statusCode(200).`when`().get("/foo")
        expect().statusCode(404).`when`().get("/not-found")
    }

    Test fun several_routes_returning_copied_responses() {
        routes {
            get("/foo") { request, response ->
                response.with {
                    body("Matched first")
                    header("X-Match-First", "Yes")
                }
            }
            get("/foo") { request, response ->
                response.with {
                    body("Matched second")
                    header("X-Match-Second", "Also yes")
                }
            }
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
            get("/foo") { request, response ->
                response {
                    body("Matched first")
                    header("X-Match-First", "Yes")
                }
            }
            get("/foo") { request, response ->
                response {
                    body("Matched second")
                    header("X-Match-Second", "Also yes")
                }
            }
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
            get("/foo") { request, response ->
                response.halt {
                    body("Matched first")
                    header("X-Match-First", "Yes")
                }
            }
            get("/foo") { request, response ->
                response.with {
                    body("Matched second")
                    header("X-Match-Second", "Also yes")
                }
            }
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
            at("foo") {
                get("bar") { request, response ->
                    response.halt {
                        body("Matched")
                    }
                }
                at("other", controller)
            }
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
            get("/order/{id}") { request, response ->
                response.with {
                    body(request.pathParameter("id").orEmpty())
                    header("X-Witness", request.pathParameter("other").orEmpty())
                }
            }
            get("/some/{float}") { request, response ->
                response.with {
                    val transactionId = request.pathParameterAs<Float>("float")
                    body(java.lang.String.format("%.2f", transactionId ?: -1f))
                    header("X-Witness", request.pathParameter("other").orEmpty())
                }
            }
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

    Test fun head_method() {
        routes {
            get("/foo") { request, response ->
                response.with {
                    body("Won't be given to HEAD")
                    header("X-Toto", "toto")
                }
            }
        }

        expect().
                body(equalTo("Won't be given to HEAD")).
                header("X-Toto", "toto").
                `when`().
                get("/foo")

        expect().
                body(equalTo("")).
                header("X-Toto", "toto").
                `when`().
                head("/foo")
    }
}