package core;

import java.util.HashMap;
import java.util.Map;

public class HttpResponseHeaders {
    private final static byte[] CRLF = Bytes.toByte("\r\n");
    private final static byte[] CONTENT_LENGTH = Bytes.toByte("Content-Length: ");
    private final static byte[] DEFAULT_CONTENT_TYPE = Bytes.toByte("Content-Type: text/plain; charset=UTF-8\r\n");
    private final static Map<String, byte[]> CONTENT_TYPE = new HashMap<String, byte[]>(){{
        put("html", Bytes.toByte("Content-Type: text/html; charset=UTF-8\r\n"));
        put("css", Bytes.toByte("Content-Type: text/css;\r\n"));
        put("gif", Bytes.toByte("Content-Type: image/gif;\r\n"));
        put("jpeg", Bytes.toByte("Content-Type: image/jpeg\r\n"));
        put("jpg", Bytes.toByte("Content-Type: image/jpeg\r\n"));
        put("png", Bytes.toByte("Content-Type: image/png;\r\n"));
        put("webp", Bytes.toByte("Content-Type: image/webp;\r\n"));
        put("ico", Bytes.toByte("Content-Type: image/x-icon;\r\n"));
        put("svg", Bytes.toByte("Content-Type: image/svg+xml;\r\n"));
        put("pdf", Bytes.toByte("Content-Type: application/pdf;\r\n"));
    }};
    private final static byte[] OK = Bytes.toByte("HTTP/1.1 200 OK\r\nConnection: close\r\n");
    private final static byte[] BAD_REQUEST = Bytes.toByte("HTTP/1.1 400 Bad Request\r\nConnection: close\r\n");
    private final static byte[] NOT_FOUND = Bytes.toByte("HTTP/1.1 404 Not Found\r\nConnection: close\r\n");
    private final static byte[] METHOD_NOT_ALLOWED = Bytes.toByte("HTTP/1.1 405 Method Not Allowed\r\nConnection: close\r\n");
    private final static byte[] URI_TOO_LONG = Bytes.toByte("HTTP/1.1 414 URI Too Long\r\nConnection: close\r\n");
    private final static byte[] REQUEST_HEADER_FIELDS_TOO_LARGE = Bytes.toByte("HTTP/1.1 431 Request Header Fields Too Large\r\nConnection: close\r\n");
    private final static byte[] INTERNAL_SERVER_ERROR = Bytes.toByte("HTTP/1.1 500 Internal Server Error\r\nConnection: close\r\n");


    public static byte[] ok(String fileExtension, long fileLength) {
        return build(OK, fileExtension, fileLength);
    }

    public static byte[] badRequest(long fileLength) {
        return build(BAD_REQUEST, "", fileLength);
    }

    public static byte[] notFound(long fileLength) {
        return build(NOT_FOUND, "", fileLength);
    }

    public static byte[] methodNotAllowed(long fileLength) {
        return build(METHOD_NOT_ALLOWED, "", fileLength);
    }

    public static byte[] uriTooLong(long fileLength) {
        return build(URI_TOO_LONG, "", fileLength);
    }

    public static byte[] requestHeaderFieldsTooLarge(long fileLength) {
        return build(REQUEST_HEADER_FIELDS_TOO_LARGE, "", fileLength);
    }

    public static byte[] internalServerError(long fileLength) {
        return build(INTERNAL_SERVER_ERROR, "", fileLength);
    }

    private static byte[] build(byte[] httpStatus, String fileExtension, long fileLength) {
        return Bytes.concat(httpStatus, CONTENT_TYPE.getOrDefault(fileExtension, DEFAULT_CONTENT_TYPE),
                CONTENT_LENGTH, Bytes.toByte(Long.toString(fileLength)), CRLF, CRLF);
    }
}
