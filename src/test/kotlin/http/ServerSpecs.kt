package http

import org.jetbrains.spek.api.Spek
import kotlin.test.assertEquals

import com.jayway.restassured.RestAssured.given
import com.jayway.restassured.RestAssured.get
import org.hamcrest.Matchers.equalTo

/**
 * @version $Id$
 */
class ServerSpecs: Spek() {

    init {
        given("a server with a single route") {
            val server = Server()
            server.routes {
                get("/foo", { request, response -> response.with {
                    body("Yes.")
                }})
            }
            server.start()

            on("hitting that route") {
                val response = get("/foo").then()
                it("should have the response defined in the route") {
                    response.assertThat().body(equalTo("Yes."));
                }
            }

            on("hitting any other route") {
                val response = get("/not-found").then()
                it("should return a 404 with no content") {
                    response.assertThat().statusCode(404);
                }
            }
        }
    }
}