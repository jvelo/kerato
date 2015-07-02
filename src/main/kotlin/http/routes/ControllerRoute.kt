package http.routes

import http.Exchange
import http.Method
import http.Request
import http.Response
import java.lang.reflect.Method as JavaMethod

/**
 * @version $Id$
 */
public class ControllerRoute(
        methods: Array<Method>,
        uri: String,
        val handler: Any
): Route(methods, uri) {

    val funs : List<Pair<Method, JavaMethod>>

    init {
        funs = handler.javaClass.getMethods().filter {
            it.getAnnotations().any() {
                val get = it as? Get
                get != null
            }
        }.map {
            Pair(Method.GET, it)
        }
    }

    override fun matches(request: Request): Boolean {
        return funs.any {
            request.method == it.first
        }
    }

    override fun apply(exchange: Exchange): Exchange {
        System.out.println("applying route ${this}")
        return exchange
    }

}