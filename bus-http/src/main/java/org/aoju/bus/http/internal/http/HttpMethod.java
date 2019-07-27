package org.aoju.bus.http.internal.http;

/**
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public final class HttpMethod {

    private HttpMethod() {
    }

    public static boolean invalidatesCache(String method) {
        return method.equals("POST")
                || method.equals("PATCH")
                || method.equals("PUT")
                || method.equals("DELETE")
                || method.equals("MOVE");     // WebDAV
    }

    public static boolean requiresRequestBody(String method) {
        return method.equals("POST")
                || method.equals("PUT")
                || method.equals("PATCH")
                || method.equals("PROPPATCH") // WebDAV
                || method.equals("REPORT");   // CalDAV/CardDAV (defined in WebDAV Versioning)
    }

    public static boolean permitsRequestBody(String method) {
        return !(method.equals("GET") || method.equals("HEAD"));
    }

    public static boolean redirectsWithBody(String method) {
        return method.equals("PROPFIND"); // (WebDAV) redirects should also maintain the request body
    }

    public static boolean redirectsToGet(String method) {
        // All requests but PROPFIND should redirect to a GET request.
        return !method.equals("PROPFIND");
    }
}
