package ru.abramov.filemanager.netty.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.nio.file.Path;
import java.nio.file.Paths;

public class NettyServer {
    private static final int PORT = 8189;
    private static Path serverPath;
    private Controller controller;
    private static Thread t;

    public static Path getServerPath() {
        return serverPath = Paths.get("./");
    }

    public static void setServerPath(Path serverPath) {
        NettyServer.serverPath = serverPath;
    }

    public NettyServer(Controller controller) {
        this.controller =controller;
        t = new Thread(() -> {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1); // пул потоков подключения клиентов
        EventLoopGroup workerGroup = new NioEventLoopGroup(); // обработка данных
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new ByteProtocolServerHandler(controller));
                        }
                    });
            ChannelFuture future = b.bind(PORT).sync(); // запуск сервера
            controller.setTfLogServer("Netty server ON");
            SqlClient.connect();
            controller.setTfLogServer("DB connected");
            future.channel().closeFuture().sync(); // остановка сервера
            SqlClient.disConnect();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
        });
        t.start();
    }

    public static void close(){
        t.interrupt();
    }
}
