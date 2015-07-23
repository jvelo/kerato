package kerato.http.routes

import kerato.http.Method
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
                val fooRoute = RequestResponseLambdaRoute(Method.GET, "/foo", { req, resp -> ok() })
                val barRoute = RequestResponseLambdaRoute(Method.GET, "/bar", { req, resp -> ok() })

                it("should match the one with the same static path and not the others") {
                    assertEquals(true, fooRoute.matches(request { path("/foo") }));
                    assertEquals(false, barRoute.matches(request { path("/foo") }));
                    assertEquals(false, fooRoute.matches(request { path("/foo/bar") }));
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
                val getRoute = ControllerRoute("/", object {
                    public get fun doGet() {
                        ok()
                    }
                })

                val postRoute = ControllerRoute("/", object {
                    public post fun doPost() {
                        ok()
                    }
                })

                val getRequest = request {
                    path("/")
                    method(Method.GET)
                }
                val postRequest = request {
                    path("/")
                    method(Method.POST)
                }

                it("should match the route with the matching method only") {
                    assertEquals(true, getRoute.matches(getRequest));
                    assertEquals(false, getRoute.matches(postRequest));

                    assertEquals(true, postRoute.matches(postRequest));
                    assertEquals(false, postRoute.matches(getRequest));
                }

                val route963 = ControllerRoute("/d-963", object {
                    public get fun doGet() {
                        ok()
                    }
                })

                val request963 = request {
                    path("/d-963")
                }

                val request96351 = request {
                    path("/d-963/d-51")
                }

                val request51 = request {
                    path("/d-51")
                }

                it("should match the route with the matching path only") {
                    assertEquals(true, route963.matches(request963));
                    assertEquals(false, route963.matches(request51));
                    assertEquals(false, route963.matches(request96351))
                }
            }

            on("adding controller route with path annotations") {
                val route1 = ControllerRoute("here", object {
                    public get("there") fun doGet() {
                        ok()
                    }
                })

                @at("up")
                class Controller {
                    public patch("there") fun doPatch() {
                        ok()
                    }
                }

                val route2 = ControllerRoute("somewhere", Controller())

                it("should account for the path annotations") {
                    assertEquals(true, route1.matches(request {
                        path("/here/there/")
                        method(Method.GET)
                    }));

                    assertEquals(false, route1.matches(request {
                        path("/here/not-there")
                        method(Method.GET)
                    }));

                    assertEquals(true, route2.matches(request {
                        path("/somewhere/up/there")
                        method(Method.PATCH)
                    }));

                    assertEquals(false, route2.matches(request {
                        path("/up/there")
                        method(Method.PATCH)
                    }));
                }
            }

            on("matching a route with path params") {
                val request = request {
                    path("/customer/123")
                }
                val routeWithRegex = RequestResponseLambdaRoute(Method.GET, "/customer/{id}", { req, resp -> ok() })
                val routeWithoutRegex = RequestResponseLambdaRoute(Method.GET, "/customer/456", { req, resp -> ok() })

                it("should match the route with passed params") {
                    assertEquals(true, routeWithRegex.matches(request));;
                    assertEquals(false, routeWithoutRegex.matches(request));;
                }
            }
        }
    }
}