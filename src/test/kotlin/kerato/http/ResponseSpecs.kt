package kerato.http

import org.jetbrains.spek.api.Spek
import kotlin.test.assertEquals

/**
 * @version $Id$
 */
class ResponseSpecs: Spek() {

    init {
        given("a response builder") {

            on("building") {
                val response = response {
                    status(Status.CONTINUE)
                }
                it("should build") {
                    assertEquals(response.status, 100)
                }
            }

            on("building from an existing response") {
                val existing = response {
                    headers(
                        "Accept" to "application/json",
                        "X-Forwared-For" to "naze.cool"
                    )
                }
                val new = existing.with {
                    status(409)
                    header("Cache-Control", "no-cache")
                }

                it ("should build a new response keeping the values it is built from") {
                    assertEquals(2, existing.headers.size())
                    assertEquals(200, existing.status)
                    assertEquals(409, new.status)
                    assertEquals(3, new.headers.size())
                }
            }
        }
    }
}