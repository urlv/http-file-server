package com.github.urlv.core;

import com.github.urlv.utils.Action;
import com.github.urlv.utils.Check;
import com.github.urlv.utils.IntWrapper;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class FileUtils {
    static class Read {
        private AsynchronousFileChannel file;
        private ByteBuffer byteBuffer;
        private long position;
        private Consumer<Integer> onSuccess;
        private Action onError;

        private Read(AsynchronousFileChannel file, ByteBuffer byteBuffer, long position) {
            this.file = file;
            this.byteBuffer = byteBuffer;
            this.position = position;
        }

        public Read onSuccess(Consumer<Integer> onSuccess){
            this.onSuccess = onSuccess;
            return this;
        }

        public Read onError(Action onError) {
            this.onError = onError;
            return this;
        }

        public void start() {
            file.read(byteBuffer, position, null, new CompletionHandler<Integer, Void>() {
                @Override
                public void completed(Integer result, Void attachment) {
                    Check.condition(result != -1)
                            .ifTrue(() -> onSuccess.accept(result))
                            .ifFalse(onError);
                }

                @Override
                public void failed(Throwable exc, Void attachment) {
                    onError.invoke();
                }
            });
        }
    }

    static class Stream {
        private AsynchronousFileChannel from;
        private AsynchronousSocketChannel to;
        private ByteBuffer byteBuffer;
        private IntWrapper position;
        private Action onSuccess;
        private Action onError;

        private Stream(AsynchronousFileChannel from, AsynchronousSocketChannel to, ByteBuffer byteBuffer, int position) {
            this.from = from;
            this.to = to;
            this.byteBuffer = byteBuffer;
            this.position = new IntWrapper().set(position);
        }

        public Stream onSuccess(Action onSuccess) {
            this.onSuccess = onSuccess;
            return this;
        }

        public Stream onError(Action onError) {
            this.onError = onError;
            return this;
        }

        public void start() {
            read(from, byteBuffer, position.get()).onError(onError).onSuccess(readResult -> {
                boolean isEOF = byteBuffer.hasRemaining();
                byteBuffer.flip();
                to.write(byteBuffer, Configuration.IO_TIMEOUT_SECONDS, TimeUnit.SECONDS, null, new CompletionHandler<Integer, Void>() {
                    @Override
                    public void completed(Integer writeResult, Void attachment) {
                        Check.condition(writeResult != -1)
                                .ifTrue(() -> Check.condition(isEOF)
                                        .ifTrue(onSuccess)
                                        .ifFalse(() -> {
                                            byteBuffer.clear();
                                            position.add(readResult);
                                            start();
                                        }))
                                .ifFalse(onError);
                    }

                    @Override
                    public void failed(Throwable exc, Void attachment) {
                        onError.invoke();
                    }
                });

            }).start();
        }
    }

    public static AsynchronousFileChannel open(File file) throws IOException {
        return AsynchronousFileChannel.open(file.toPath());
    }

    public static Read read(AsynchronousFileChannel file, ByteBuffer byteBuffer, long position) {
        return new Read(file, byteBuffer, position);
    }

    public static Stream stream(AsynchronousFileChannel from, AsynchronousSocketChannel to, ByteBuffer byteBuffer, int position) {
        return new Stream(from, to, byteBuffer, position);
    }

    public static void close(AsynchronousFileChannel file) {
        try {
            file.close();
        } catch (IOException e) {
            // nothing
        }
    }
}
