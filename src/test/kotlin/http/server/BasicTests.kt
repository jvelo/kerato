package http.server

import com.jayway.restassured.http.ContentType.JSON
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
}