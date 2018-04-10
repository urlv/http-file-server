package core;

import utils.Check;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class SocketUtils {
    public static void read(AsynchronousSocketChannel client, ByteBuffer byteBuffer, Consumer<Integer> onSuccess) {
        client.read(byteBuffer, Configuration.IO_TIMEOUT_SECONDS, TimeUnit.SECONDS, null, new CompletionHandler<Integer, Void>() {
            @Override
            public void completed(Integer result, Void attachment) {
                Check.condition(result != -1)
                        .ifTrue(() -> onSuccess.accept(result))
                        .ifFalse(() -> SocketUtils.close(client));
            }

            @Override
            public void failed(Throwable exc, Void attachment) {
                SocketUtils.close(client);
            }
        });
    }

    public static void write(AsynchronousSocketChannel client, ByteBuffer byteBuffer, Consumer<Integer> onSuccess) {
        client.write(byteBuffer, Configuration.IO_TIMEOUT_SECONDS, TimeUnit.SECONDS, null, new CompletionHandler<Integer, Void>() {
            @Override
            public void completed(Integer result, Void attachment) {
                Check.condition(result != -1)
                        .ifTrue(() -> onSuccess.accept(result))
                        .ifFalse(() -> SocketUtils.close(client));
            }

            @Override
            public void failed(Throwable exc, Void attachment) {
                SocketUtils.close(client);
            }
        });
    }

    public static void close(AsynchronousSocketChannel client) {
        try {
            client.shutdownOutput().shutdownInput().close();
        } catch (IOException e) {
            // nothing
        }
    }
}
