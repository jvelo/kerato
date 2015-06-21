package http

/**
 * @version $Id$
 */

public data class Response(
        val status: Int,
        val headers: Map<String, String>,
        val body: Any
) {
    inline fun with(fn: ResponseBuilder.() -> Unit): Response {
        val builder = ResponseBuilder(this)
        builder.fn()
        return builder.build()
    }
}

class ResponseBuilder() {

    constructor(other: Response) : this() {
        this.status = other.status
        this.headers.putAll(other.headers)
    }

    private var status: Int = Status.OK.code
    private val headers: MutableMap<String, String> = hashMapOf()
    private var body: Any = ""

    fun status(status: Status) {
        this.status = status.code
    }

    fun status(status: Int) {
        this.status = status
    }

    fun body(body: Any) {
        this.body = body
    }

    fun header(name: String, value: String) {
        this.headers.putAll(Pair(name, value))
    }

    fun header(header: Pair<String, String>) {
        this.headers.put(header.first, header.second)
    }

    fun headers(vararg headers: Pair<String, String>) {
        for (header in headers) {
            this.header(header)
        }
    }

    fun build(): Response {
        return Response(
                status = this.status,
                headers = this.headers,
                body = this.body
        )
    }

}

inline fun response(fn: ResponseBuilder.() -> Unit): Response {
    val builder = ResponseBuilder()
    builder.fn()
    return builder.build()
}

enum class Status(val code: Int) {

    // 1xx

    CONTINUE(100),
    SWITCHING_PROTOCOLS(101),
    PROCESSING(102),

    // 2xx

    OK(200),
    CREATED(201),
    ACCEPTED(202),
    NON_AUTHORITATIVE(203),
    NO_CONTENT(204),
    RESET_CONTENT(205),
    PARTIAL_CONTENT(206),
    MULTI_STATUS(207),
    ALREADY_REPORTED(208),
    IM_USED(226),

    // 3xx

    MULTIPLE_CHOICES(300),
    MOVED_PERMANENTLY(301),
    FOUND(302),
    SEE_OTHER(303),
    NOT_MODIFIED(304),
    USE_PROXY(305),
    SWITCH_PROXY(306),
    TEMPORARY_REDIRECT(307),
    PERMANENT_REDIRECT(308),

    // 4xx

    BAD_REQUEST(400),
    UNAUTHORIZED(401),
    PAYMENT_REQUIRED(402),
    FORBIDDEN(403),
    NOT_FOUND(404),
    METHOD_NOT_ALLOWED(405),
    NOT_ACCEPTABLE(406),
    PROXY_AUTHENTICATION_REQUIRED(407),
    REQUEST_TIMEOUT(408),
    CONFLICT(409),
    GONE(410),
    LENGTH_REQUIRED(411),
    PRECONDITION_FAILED(412),
    REQUEST_ENTITY_TOO_LARGE(413),
    REQUEST_URI_TOO_LONG(414),
    UNSUPPORTED_MEDIA_TYPE(415),
    REQUEST_RANGE_NOT_SATISFIABLE(416),
    EXPECTATION_FAILED(417),
    I_AM_A_TEAPOT(418),
    AUTHENTICATION_TIMEOUT(419),

    MISREDIRECTED_REQUEST(421),
    UNPROCESSABLE_ENTITY(422),
    LOCKED(423),
    FAILED_DEPENDENCY(424),

    UPGRADE_REQUIRED(426),

    PRECONDITION_REQUIRED(428),
    TOO_MANY_REQUESTS(429),

    REQUEST_HEADER_FIELDS_TOO_LARGE(431),

    UNAVAILABLE_FOR_LEGAL_REASONS(451),

    // 5xx

    INTERNAL_SERVER_ERROR(500),
    NOT_IMPLEMENTED(501),
    BAD_GATEWAY(502),
    SERVICE_UNAVAILABLE(503),
    GATEWAY_TIMEOUT(504),
    HTTP_VERSION_NOT_SUPPORTED(505),
    VARIANT_ALSO_NEGOTIATES(506),
    INSUFFICIENT_STORAGE(507),
    LOOP_DETECTED(508),

    NOT_EXTENDED(510),
    NETWORK_AUTHENTICATION_REQUIRED(511)
}