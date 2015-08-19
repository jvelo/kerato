package kerato.http.server

import com.jayway.restassured.config.RedirectConfig.redirectConfig
import com.jayway.restassured.config.RestAssuredConfig.newConfig
import kerato.http.Request
import kerato.http.Response
import kerato.http.response
import kerato.http.routes.get
import kerato.http.seeOther
import org.hamcrest.Matchers.equalTo
import org.junit.Test

/**
 * @version $Id$
 */
public class ControllerTests : RestTests() {

    Test fun a_controller_route_returning_a_response() {
        routes {
            at("/are-you-there", object {
                public get fun testGet() : Response {
                    return seeOther("/i-am-not-there")
                }
            })
        }
        given().
                config(newConfig().redirect(redirectConfig().followRedirects(false))).
        expect().
                statusCode(303).
                header("Location", "/i-am-not-there").
        `when`().
                get("/are-you-there")

    }

    Test fun controller_with_methods_by_name() {
        val controller = object {
            public fun get(): String {
                return "gotcha"
            }
            public fun post(): String {
                return "posted"
            }
        }

        routes {
            at("/test", controller)
        }

        expect().
                body(equalTo("gotcha")).
        `when`().
                get("/test")

        expect().
                body(equalTo("posted")).
        `when`().
                post("/test")
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