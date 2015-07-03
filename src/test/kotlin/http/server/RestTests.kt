package http.server

import com.jayway.restassured.specification.RequestSpecification
import com.jayway.restassured.RestAssured.given as stated
import com.jayway.restassured.specification.ResponseSpecification
import http.Server
import http.routes.Routes
import org.junit.Before
import org.junit.Rule
import org.junit.rules.ExpectedException
import java.util.Random

/**
 * @version $Id$
 */
public open class RestTests {
    val _thrown: ExpectedException = ExpectedException.none()
    val server = Server()
    val port = 1000 + Random().nextInt(8999)

    Rule
    public fun getThrown(): ExpectedException = _thrown

    var responseSpec = null : ResponseSpecification?

    Before fun initialize() {

    }

    init {
        server.configure {
            port(port)
        }
        responseSpec = given().port(port).expect()
        server.start()
    }

    public fun routes(fn: Routes.() -> Unit) {
        server.configure {
            routes {
                fn()
            }
        }
    }

    public fun expect() : ResponseSpecification {
        return stated().port(port).expect()
    }

    public fun given() : RequestSpecification {
        return stated().port(port)
    }
}