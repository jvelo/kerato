package kerato.http.routes

import kerato.http.*

/**
 * @version $Id$
 */
public class RequestResponseLambdaRoute(val handler: (request: Request, response: Response) -> Response) : (Exchange) -> Exchange {

    override fun invoke(exchange: Exchange): Exchange {

        val result = this.handler(exchange.request, exchange.response)
        val response = when (result) {
            is CopiedResponse -> result
            else -> response {
                merge(exchange.response)
                merge(result)
            }
        }
        return Exchange(exchange.request, response)
    }
}