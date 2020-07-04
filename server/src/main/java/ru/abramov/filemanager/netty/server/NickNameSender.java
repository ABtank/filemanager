package ru.abramov.filemanager.netty.server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;

import java.nio.charset.StandardCharsets;

public class NickNameSender {

    public static void send(String nickname, Channel channel) {
        ByteBuf buf = null;
//       длинна никнейма
        buf = ByteBufAllocator.DEFAULT.directBuffer(4);
        buf.writeInt(nickname.length());
        channel.writeAndFlush(buf);
//        никнейм
        byte[] loginBytes = nickname.getBytes(StandardCharsets.UTF_8);
        buf = ByteBufAllocator.DEFAULT.directBuffer(loginBytes.length);
        buf.writeBytes(loginBytes);
        channel.writeAndFlush(buf);
    }
}