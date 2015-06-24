package http.routes

import http.Method
import http.ok
import http.request
import org.jetbrains.spek.api.Spek
import kotlin.test.assertEquals

/**
 * @version $Id$
 */
public class RouteMatcherSpecs : Spek() {
    init {
        given("a route matcher") {
            val matcher = RouteMatcher()

            on("matching a route based on a static path") {
                val request = request {
                    path("/foo")
                }
                val fooRoute = RequestResponseLambdaRoute(Method.GET, "/foo", { req, resp -> ok() })
                val barRoute = RequestResponseLambdaRoute(Method.GET, "/bar", { req, resp -> ok() })

                it("should match the one with the same static path and not the others") {
                    assertEquals(true, matcher.matches(request, fooRoute));
                    assertEquals(false, matcher.matches(request, barRoute));
                }
            }

            on("matching a route based on a method") {
                val getRequest = request {
                    path("/")
                    method(Method.GET)
                }
                val postRequest = request {
                    path("/")
                    method(Method.POST)
                }

                val route = RequestResponseLambdaRoute(Method.POST, "/", { req, resp -> ok() })

                it("should match only when the method matches") {
                    assertEquals(true, matcher.matches(postRequest, route));
                    assertEquals(false, matcher.matches(getRequest, route));
                }
            }

            on("matching a route based on an array of methods") {
                val postRequest = request {
                    path("/")
                    method(Method.POST)
                }

                val routeWithOneEntryArray = RequestResponseLambdaRoute(arrayOf(Method.POST), "/", { req, resp -> ok() })
                val routeWithOneTwoArray = RequestResponseLambdaRoute(arrayOf(Method.POST, Method.GET), "/", { req, resp -> ok() })
                val routeWithoutPostRequest = RequestResponseLambdaRoute(arrayOf(Method.OPTIONS), "/", { req, resp -> ok() })

                it("should match only when the method matches") {
                    assertEquals(true, matcher.matches(postRequest, routeWithOneEntryArray));
                    assertEquals(true, matcher.matches(postRequest, routeWithOneTwoArray));
                    assertEquals(false, matcher.matches(postRequest, routeWithoutPostRequest));
                }
            }

        }
    }
}