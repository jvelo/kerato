package http.server

import com.jayway.restassured.http.ContentType.JSON
import http.Response
import http.Status
import http.ok
import http.response
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
        val controller = object  {
            public post fun method() : Response {
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
}