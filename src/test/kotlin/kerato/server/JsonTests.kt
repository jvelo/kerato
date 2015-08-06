package kerato.http.server

import com.jayway.restassured.http.ContentType
import org.hamcrest.Matchers
import org.junit.Test

/**
 * @version $Id$
 */
public class JsonTests : RestTests() {

    Test fun a_route_returning_json() {
        routes {
            get("/foo") { request, response ->
                response.with {
                    json(mapOf("hello" to "world"))
                }
            }
        }

        expect()
                .body("hello", Matchers.equalTo("world"))
                .contentType(ContentType.JSON)
                .`when`()
                .get("/foo")
    }

    Test fun a_route_returning_json_via_vararg() {
        routes {
            get("/foo") { request, response ->
                response.with {
                    json("hello" to "world")
                }
            }
            get("/bar") { request, response ->
                response.with {
                    json(
                        "hello" to "world",
                        "hallo" to "welt"
                    )
                }
            }
        }
        expect()
                .body("hello", Matchers.equalTo("world"))
                .contentType(ContentType.JSON)
                .`when`()
                .get("/foo")

        expect()
                .body("hello", Matchers.equalTo("world"))
                .body("hallo", Matchers.equalTo("welt"))
                .contentType(ContentType.JSON)
                .`when`()
                .get("/bar")
    }
}