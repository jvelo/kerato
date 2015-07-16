package http.routes

import http.Method
import http.ok
import http.request
import org.jetbrains.spek.api.Spek
import kotlin.test.assertEquals

/**
 * @version $Id$
 */
public class RouteMatchingSpecs : Spek() {
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
                val getRoute = ControllerRoute(Method.values(), "/", object {
                    public get fun doGet() {
                        ok()
                    }
                })

                val postRoute = ControllerRoute(Method.values(), "/", object {
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

                val route963 = ControllerRoute(Method.values(), "/d-963", object {
                    public get fun doGet() {
                        ok()
                    }
                })

                val request963 = request {
                    path("/d-963")
                }

                val request51 = request {
                    path("/d-51")
                }

                it("should match the route with the matching path only") {
                    assertEquals(true, route963.matches(request963));
                    assertEquals(false, route963.matches(request51));
                }
            }

            on("adding controller route with path annotations") {
                val route = ControllerRoute(Method.values(), "here", object {
                    public get("there") fun doGet() {
                        ok()
                    }
                })

                it("should account for the path annotations") {
                    assertEquals(true, route.matches(request {
                        path("/here/there/")
                        method(Method.GET)
                    }));

                    assertEquals(false, route.matches(request {
                        path("/here/not-there")
                        method(Method.GET)
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