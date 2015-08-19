package kerato.http.server

import com.jayway.restassured.specification.RequestSpecification
import com.jayway.restassured.specification.ResponseSpecification
import kerato.http.Server
import kerato.http.routes.RoutesBuilder
import org.junit.Rule
import org.junit.rules.ExpectedException
import java.util.Random
import com.jayway.restassured.RestAssured.given as stated

/**
 * @version $Id$
 */
public open class RestTests {
    val _thrown: ExpectedException = ExpectedException.none()
    val server = Server()
    val port = 1000 + Random().nextInt(8999)

    Rule
    public fun getThrown(): ExpectedException = _thrown

    init {
        server.configure {
            port(port)
        }
        server.start()
    }

    public fun routes(fn: RoutesBuilder.() -> Unit) {
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