package http

/**
 * @version $Id$
 */
public interface Handler {
    fun handle(request: Request, response:Response) : Response;
}