package core;

import utils.Action;
import utils.Check;
import utils.IntWrapper;
import utils.Util;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class RequestHandler {
    private final static byte[] HTTP_REQUEST_HEADER_PREFIX = Bytes.toByte("GET ");
    private final static byte[] SPACE = Bytes.toByte(" ");
    private static final int HEADERS_MAX_SIZE = 2048; // 1024 * 2 = 2kb
    private static final int READ_FILE_BUFFER_MAX_SIZE = 524288; // 1024 * 512 = 512kb

    private Consumer<Integer> onSuccessReadUrl; // define here(in class level) to resolve recursion lambda problem.
    private Consumer<Integer> onSuccessReadAllHeaders; // same as above ^.

    private AsynchronousSocketChannel client;
    private String url;

    public RequestHandler(AsynchronousSocketChannel client) {
        this.client = client;
        this.url = "";
    }

    public void start() {
        checkHttpMethod();
    }

    private void checkHttpMethod() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(HTTP_REQUEST_HEADER_PREFIX.length);

        SocketUtils.read(client, byteBuffer, numBytes ->
                Check.condition(Bytes.equals(byteBuffer.array(), HTTP_REQUEST_HEADER_PREFIX))
                        .ifTrue(this::readUrl)
                        .ifFalse(() ->
                                SocketUtils.write(client, ByteBuffer.wrap(HttpResponseHeaders.methodNotAllowed(0)),
                                        num -> SocketUtils.close(client))));
    }

    private void readUrl() {
        IntWrapper headersSize = new IntWrapper();
        ByteBuffer byteBuffer = ByteBuffer.allocate(128);

        onSuccessReadUrl = numBytes -> {
            if (Bytes.contains(byteBuffer.array(), SPACE)) {
                url += Bytes.toString(Bytes.split(byteBuffer.array(), SPACE)[0]);
                url = Util.decodeUrl(url);
                byteBuffer.clear();
                readRemainingHeaders(byteBuffer, headersSize);
            } else if (headersSize.add(numBytes).get() > HEADERS_MAX_SIZE) {
                SocketUtils.write(client, ByteBuffer.wrap(HttpResponseHeaders.uriTooLong(0)),
                        num -> SocketUtils.close(client));
            } else if (!byteBuffer.hasRemaining()) {
                url += Bytes.toString(byteBuffer.array());
                byteBuffer.clear();
                SocketUtils.read(client, byteBuffer, onSuccessReadUrl);
            } else {
                SocketUtils.write(client, ByteBuffer.wrap(HttpResponseHeaders.badRequest(0)),
                        num -> SocketUtils.close(client));
            }
        };

        SocketUtils.read(client, byteBuffer, onSuccessReadUrl);
    }

    private void readRemainingHeaders(ByteBuffer byteBuffer, IntWrapper headersSize) {
        // Needs this method mainly to enable client close socket gracefully
        onSuccessReadAllHeaders = (numBytes) -> {
            if (headersSize.add(numBytes).get() > HEADERS_MAX_SIZE) {
                SocketUtils.write(client, ByteBuffer.wrap(HttpResponseHeaders.requestHeaderFieldsTooLarge(0)),
                        num -> SocketUtils.close(client));
            } else if (!byteBuffer.hasRemaining()) {
                byteBuffer.clear();
                SocketUtils.read(client, byteBuffer, onSuccessReadAllHeaders);
            } else {
                writeResponseHeaders();
            }
        };

        SocketUtils.read(client, byteBuffer, onSuccessReadAllHeaders);
    }

    private void writeResponseHeaders() {
        File file = new File(Configuration.ROOT_DIR, Util.subString(url, "?"));
        Predicate<File> isNotFound = f -> {
            try {
                return !f.getCanonicalPath().startsWith(Configuration.ROOT_DIR) || !f.isFile(); // preventing path traversal
            } catch (IOException e) {
                return true;
            }
        };

        Check.condition(isNotFound, file).ifTrue(() -> {
            SocketUtils.write(client, ByteBuffer.wrap(HttpResponseHeaders.notFound(0)),
                    (numBytes) -> SocketUtils.close(client));
        }).ifFalse(() -> {
            try {
                AsynchronousFileChannel fileChannel = FileUtils.open(file);
                ByteBuffer byteBuffer = ByteBuffer.allocate(file.length() < READ_FILE_BUFFER_MAX_SIZE ?
                        (int)file.length() + 1 : READ_FILE_BUFFER_MAX_SIZE);
                IntWrapper position = new IntWrapper().set(0);

                SocketUtils.write(client, ByteBuffer.wrap(HttpResponseHeaders.ok(Util.getFileExtension(file.toString()), file.length())),
                        (numBytes) -> sendFile(fileChannel, byteBuffer, position));
            } catch (IOException e) {
                SocketUtils.write(client, ByteBuffer.wrap(HttpResponseHeaders.internalServerError(0)),
                        (numBytes) -> SocketUtils.close(client));
            }
        });
    }

    private void sendFile(AsynchronousFileChannel file,
                          ByteBuffer byteBuffer, IntWrapper position) {
        Action closeResources = () -> {
            FileUtils.close(file);
            SocketUtils.close(client);
        };

        FileUtils.stream(file, client, byteBuffer, position.get()).onSuccess(closeResources).onError(closeResources).start();
    }
}
