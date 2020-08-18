package ru.abramov.filemanager.network;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.channel.socket.SocketChannel;
import ru.abramov.filemanager.common.StringSender;

public class NettyClient {
    private SocketChannel channel;

    public SocketChannel getChannel() {
        return channel;
    }

    private static String host;
    private static final int PORT = 8189;

    public NettyClient(String login, String password, String host, String nickname) {
        NettyClient.host = host;
        Thread t = new Thread(() -> {
            EventLoopGroup workerGroup = new NioEventLoopGroup();
            try {
                Bootstrap b = new Bootstrap();
                b.group(workerGroup)
                        .channel(NioSocketChannel.class)
                        .handler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel socketChannel) throws Exception {
                                channel = socketChannel;
                                socketChannel.pipeline().addLast(new ByteProtocolClientHandler()
                                );
                            }
                        });
                ChannelFuture future = b.connect(host, PORT).sync();
                if(nickname.length() == 0)StringSender.sendAuth(login, password, channel);
                else StringSender.sendChangeNickname(login,password,channel,nickname);
                future.channel().closeFuture().sync();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                workerGroup.shutdownGracefully();
            }
        });
        t.setDaemon(true);
        t.start();
    }

    public void sendMessage(String str) {
        channel.writeAndFlush(str);
    }

    public void close() {
        channel.close();
    }
}
