package com.github.urlv;

import com.github.urlv.core.Configuration;
import com.github.urlv.core.RequestHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

public class HttpFileServer {
    private int port;
    private AsynchronousServerSocketChannel server;

    public HttpFileServer(int port, String dir) {
        this.port = port;
        Configuration.ROOT_DIR = dir;
    }

    public void start() throws IOException {
        if (server != null && server.isOpen()) {
            throw new IllegalStateException("The server is already running");
        }

        server = AsynchronousServerSocketChannel.open();
        server.bind(new InetSocketAddress(port)).accept(null, new CompletionHandler<AsynchronousSocketChannel, Void>() {
            @Override
            public void completed(AsynchronousSocketChannel client, Void attachment) {
                server.accept(null, this);
                new RequestHandler(client).start();
            }

            @Override
            public void failed(Throwable exc, Void attachment) {
                // An error occurred while trying to get a new connection
            }
        });
    }

    public void close() throws IOException {
        if (server == null || !server.isOpen()) {
            throw new IllegalStateException("There is no running server to close");
        } else {
            server.close();
        }
    }
}
