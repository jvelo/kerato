package http.routes

import http.Method
import http.ok
import http.request
import org.jetbrains.spek.api.Spek
import kotlin.test.assertEquals

/**
 * @version $Id$
 *
 * TODO merge with route specs
 */
public class RouteMatcherSpecs : Spek() {
    init {
        given("a route matcher") {

            on("matching a route based on a static path") {
                val request = request {
                    path("/foo")
                }
                val fooRoute = RequestResponseLambdaRoute(Method.GET, "/foo", { req, resp -> ok() })
                val barRoute = RequestResponseLambdaRoute(Method.GET, "/bar", { req, resp -> ok() })

                it("should match the one with the same static path and not the others") {
                    assertEquals(true, fooRoute.matches(request));
                    assertEquals(false, barRoute.matches(request));
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
                    assertEquals(true, route.matches(postRequest));
                    assertEquals(false, route.matches(getRequest));
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
                    assertEquals(true, routeWithOneEntryArray.matches(postRequest));
                    assertEquals(true, routeWithOneTwoArray.matches(postRequest));
                    assertEquals(false, routeWithoutPostRequest.matches(postRequest));
                }
            }

            on("adding controller route as object instance") {
                public class Controller {
                    public Get fun doGet() {
                        ok()
                    }
                }
                val route = ControllerRoute(Method.values(), "/customer", Controller())

                val getRequest = request {
                    path("/")
                    method(Method.GET)
                }
                val postRequest = request {
                    path("/")
                    method(Method.POST)
                }

                it("should match the route with a get method only") {
                    assertEquals(true, route.matches(getRequest));
                    assertEquals(false, route.matches(postRequest));
                }
            }
        }
    }
}