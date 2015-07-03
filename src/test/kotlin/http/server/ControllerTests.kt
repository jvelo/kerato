package http.server

import com.jayway.restassured.http.ContentType
import com.jayway.restassured.config.RestAssuredConfig.newConfig
import com.jayway.restassured.config.RedirectConfig.redirectConfig

import http.Response
import http.routes.get
import http.seeOther
import org.hamcrest.Matchers
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
}