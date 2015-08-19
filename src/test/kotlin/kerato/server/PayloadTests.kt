package kerato.http.server

import org.hamcrest.Matchers.equalTo
import org.junit.Test

/**
 * @version $Id$
 */
public class PayloadTests : RestTests() {
    Test fun a_route_returning_json() {
        routes {
            post("/foo") { request, response ->
                response.with {
                    body(request.content.orEmpty())
                }
            }
        }

        given()
                .body("tada").
        expect()
                .body(equalTo("tada")).
        `when`()
                .post("/foo")

    }
}