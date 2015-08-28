package kerato.http.routes

import kerato.http.Exchange
import kerato.http.HttpMethod
import kerato.http.ok
import kerato.http.request
import org.jetbrains.spek.api.Spek
import kotlin.test.assertEquals

/**
 * @version $Id$
 */
public class RouteMatchingSpecs : Spek() {
    init {
        given("a route matcher") {

            on("matching a route based on a static path") {
                val fooRoute = RouteEntry(HttpMethod.GET, "/foo")
                val barRoute = RouteEntry(HttpMethod.GET, "/bar")

                it("should match the one with the same static path and not the others") {
                    assertEquals(true, fooRoute.matches(request { path("/foo") }));
                    assertEquals(false, barRoute.matches(request { path("/foo") }));
                    assertEquals(false, fooRoute.matches(request { path("/foo/bar") }));
                }
            }

            on("matching a route based on a method") {
                val getRequest = request {
                    path("/")
                    method(HttpMethod.GET)
                }
                val postRequest = request {
                    path("/")
                    method(HttpMethod.POST)
                }

                val route = RouteEntry(HttpMethod.POST, "/")

                it("should match only when the method matches") {
                    assertEquals(true, route.matches(postRequest));
                    assertEquals(false, route.matches(getRequest));
                }
            }


            on("matching a route with path params") {
                val request = request {
                    path("/customer/123")
                }
                val routeWithRegex = RouteEntry(HttpMethod.GET, "/customer/{id}")
                val routeWithoutRegex = RouteEntry(HttpMethod.GET, "/customer/456")

                it("should match the route with passed params") {
                    assertEquals(true, routeWithRegex.matches(request));;
                    assertEquals(false, routeWithoutRegex.matches(request));;
                }
            }
        }
    }
}