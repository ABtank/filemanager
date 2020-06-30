package ru.abramov.filemanager.netty.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

public class NettyServer {
    private static final int PORT = 8189;


    public static void main(String[] args) {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1); // пул потоков подключения клиентов
        EventLoopGroup workerGroup = new NioEventLoopGroup(); // обработка данных
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new StringDecoder(), new StringEncoder(), new MainHandler());
                        }
                    });
            ChannelFuture future = b.bind(PORT).sync(); // запуск сервера
            System.out.println("Netty server ON");
            SqlClient.connect();
            future.channel().closeFuture().sync(); // остановка сервера
            SqlClient.disConnect();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }

    }
}
