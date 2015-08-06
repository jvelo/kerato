package kerato.http.server

import com.jayway.restassured.http.ContentType
import com.jayway.restassured.http.ContentType.JSON
import kerato.http.Response
import kerato.http.Request
import kerato.http.Status
import kerato.http.ok
import kerato.http.response
import kerato.http.routes.get
import kerato.http.routes.post
import kerato.http.server.RestTests
import org.hamcrest.Matchers
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.nullValue
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